package com.lms.multimedia.service.impl;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.util.UlidGenerator;
import com.lms.multimedia.config.StorageProperties;
import com.lms.multimedia.dto.FileDownload;
import com.lms.multimedia.dto.FileResponse;
import com.lms.multimedia.entity.File;
import com.lms.multimedia.entity.FileStatus;
import com.lms.multimedia.mapper.FileMapper;
import com.lms.multimedia.repository.FileRepository;
import com.lms.multimedia.service.FileService;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final long MAX_PRESIGN_SECONDS = Duration.ofDays(7).toSeconds();

    private final FileRepository fileRepository;
    private final MinioClient minioClient;
    private final StorageProperties storageProperties;
    private final FileMapper fileMapper;

    @Override
    @Transactional
    public FileResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "File is required");
        }
        String fileId = UlidGenerator.nextUlid();
        String originalName = file.getOriginalFilename();
        String objectKey = buildObjectKey(fileId, originalName);
        String mimeType = file.getContentType();
        String bucket = getBucketByMimeType(mimeType);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(mimeType)
                    .build());
        } catch (Exception ex) {
            log.error("Failed to upload object {} to bucket {}: {}", objectKey, bucket, ex.getMessage());
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Unable to upload file", ex);
        }

        File entity = new File();
        entity.setId(fileId);
        entity.setObjectKey(objectKey);
        entity.setFilename(originalName == null || originalName.isBlank() ? "file" : originalName);
        entity.setMimeType(mimeType == null ? "application/octet-stream" : mimeType);
        entity.setSizeBytes(file.getSize());
        entity.setStatus(FileStatus.PENDING.name());
        File saved = fileRepository.save(entity);
        return fileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownload download(String fileId) {
        File file = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "File not found"));

        Optional<StatObjectResponse> statResponse = statObject(file);
        if (statResponse.isEmpty()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "File object not found");
        }

        String bucket = getBucketByMimeType(file.getMimeType());
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(file.getObjectKey())
                    .build());
            StatObjectResponse stat = statResponse.get();
            return new FileDownload(
                    stream,
                    file.getFilename(),
                    file.getMimeType(),
                    stat.size());
        } catch (Exception ex) {
            log.error("Failed to get object {}: {}", file.getObjectKey(), ex.getMessage());
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Unable to download file", ex);
        }
    }

    @Override
    @Transactional
    public void commit(String fileId) {
        File file = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "File not found"));

        Optional<StatObjectResponse> statResponse = statObject(file);
        if (statResponse.isEmpty()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Uploaded object not found");
        }
        StatObjectResponse stat = statResponse.get();
        if (file.getSizeBytes() == null) {
            file.setSizeBytes(stat.size());
        }
        file.setStatus(FileStatus.READY.name());
        File saved = fileRepository.save(file);
        fileMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void cleanupPending() {
        Instant threshold = Instant.now().minus(storageProperties.getPendingTtl());
        List<File> expired = fileRepository
                .findByStatusAndUpdatedAtBeforeAndDeletedFalse(FileStatus.PENDING.name(), threshold);
        if (expired.isEmpty()) {
            return;
        }
        expired.forEach(file -> {
            if (storageProperties.isDeleteObjectOnSoftDelete()) {
                removeObjectQuietly(getBucketByMimeType(file.getMimeType()), file.getObjectKey());
            }
        });
        fileRepository.deleteAll(expired);
    }

    private Optional<StatObjectResponse> statObject(File file) {
        String bucket = getBucketByMimeType(file.getMimeType());
        try {
            StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(file.getObjectKey())
                    .build());
            return Optional.ofNullable(response);
        } catch (Exception ex) {
            log.warn("Object not found for file {}: {}", file.getId(), ex.getMessage());
            return Optional.empty();
        }
    }

    private void removeObjectQuietly(String bucket, String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to remove object {} from bucket {} in MinIO: {}", objectKey, bucket, ex.getMessage());
        }
    }

    private String getBucketByMimeType(String mimeType) {
        if (mimeType == null) {
            return storageProperties.getMinio().getBucket();
        }
        if (mimeType.startsWith("image/")) {
            return storageProperties.getMinio().getImageBucket();
        }
        if (mimeType.startsWith("audio/")) {
            return storageProperties.getMinio().getAudioBucket();
        }
        return storageProperties.getMinio().getBucket();
    }

    private String buildObjectKey(String fileId, String filename) {
        String sanitizedFileName = sanitizeFileName(filename);
        return "files/" + fileId + "/" + sanitizedFileName;
    }

    private String sanitizeFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }
        return filename.trim().replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
