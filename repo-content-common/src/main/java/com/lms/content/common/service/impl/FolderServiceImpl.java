package com.lms.content.common.service.impl;

import com.lms.common.event.PackageStatusEvent;
import com.lms.common.event.PackageStatusPublisher;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.dto.request.CreateFolderRequest;
import com.lms.content.common.dto.request.UpdateFolderRequest;
import com.lms.content.common.dto.response.FolderResponse;
import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.entity.enums.PublishStatus;
import com.lms.content.common.mapper.FolderMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.content.common.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final PackageRepository packageRepository;
    private final StudySetRepository studySetRepository;
    private final FolderMapper folderMapper;
    private final PackageStatusPublisher packageStatusPublisher;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Override
    public FolderResponse createFolder(CreateFolderRequest request, String userId) {
        log.info("Creating folder for user: {}", userId);

        validateCreateFolder(request, userId);

        Folder folder = folderMapper.toEntity(request);
        folder.setUserId(userId);

        if (request.getPackageId() != null) {
            Package packageEntity = packageRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));
            folder.setPackageEntity(packageEntity);
        }

        beforeSaveFolder(folder, request);

        Folder saved = folderRepository.save(folder);

        // Nội dung mới tạo trong package → revert package về DRAFT
        if (saved.getPackageEntity() != null) {
            revertPackageToDraft(saved.getPackageEntity(), userId);
        }

        afterSaveFolder(saved);

        return folderMapper.toResponse(saved);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public FolderResponse getFolderById(String id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));
        return folderMapper.toResponse(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getFoldersByUserId(String userId) {
        List<Folder> folders = folderRepository.findByUserId(userId);
        return folderMapper.toResponseList(folders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getFoldersByPackageId(String packageId) {
        List<Folder> folders = folderRepository.findByPackageId(packageId);
        return folderMapper.toResponseList(folders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getAllFolders() {
        List<Folder> folders = folderRepository.findAll();
        return folderMapper.toResponseList(folders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getFoldersByStudySetId(String studySetId) {
        List<Folder> folders = folderRepository.findByStudySetId(studySetId);
        return folderMapper.toResponseList(folders);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Override
    public FolderResponse updateFolder(String id, UpdateFolderRequest request, String userId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        validateUpdateFolder(folder, request, userId);

        if (request.getName() != null) {
            folder.setName(request.getName());
        }
        if (request.getDescription() != null) {
            folder.setDescription(request.getDescription());
        }
        if (request.getIsPrivate() != null) {
            folder.setPrivate(request.getIsPrivate());
        }

        beforeUpdateFolder(folder, request);

        Folder updated = folderRepository.save(folder);

        // Nội dung trong package bị sửa → revert package về DRAFT
        if (updated.getPackageEntity() != null) {
            revertPackageToDraft(updated.getPackageEntity(), userId);
        }

        afterUpdateFolder(updated);

        return folderMapper.toResponse(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Override
    public void deleteFolder(String id, String userId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Lưu thông tin package trước khi xóa folder
        Package parentPackage = folder.getPackageEntity();

        beforeDeleteFolder(folder, userId);

        folderRepository.delete(folder);

        // Nội dung xóa khỏi package → revert package về DRAFT
        if (parentPackage != null) {
            // Reload để tránh dùng detached entity
            packageRepository.findById(parentPackage.getId())
                    .ifPresent(pkg -> revertPackageToDraft(pkg, userId));
        }

        afterDeleteFolder(id, userId);
    }

    // ── STUDY SET MANAGEMENT ──────────────────────────────────────────────────

    @Override
    public FolderResponse addStudySetToFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        folder.addStudySet(studySet);
        Folder updated = folderRepository.save(folder);

        // Nội dung thêm vào folder → revert package về DRAFT
        if (updated.getPackageEntity() != null) {
            revertPackageToDraft(updated.getPackageEntity(), userId);
        }

        return folderMapper.toResponse(updated);
    }

    @Override
    public FolderResponse removeStudySetFromFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        folder.removeStudySet(studySet);
        Folder updated = folderRepository.save(folder);

        // Nội dung xóa khỏi folder → revert package về DRAFT
        if (updated.getPackageEntity() != null) {
            revertPackageToDraft(updated.getPackageEntity(), userId);
        }

        return folderMapper.toResponse(updated);
    }

    @Override
    public void updateFolderPrivacy(String folderId, boolean isPrivate, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        folder.setPrivate(isPrivate);
        Folder updated = folderRepository.save(folder);

        // Thay đổi privacy → revert package về DRAFT
        if (updated.getPackageEntity() != null) {
            revertPackageToDraft(updated.getPackageEntity(), userId);
        }
    }

    // ── REVERT HELPER ─────────────────────────────────────────────────────────

    /**
     * Nếu package đang PUBLISHED, tự động revert về DRAFT và broadcast Kafka event.
     * Idempotent — nếu đã DRAFT thì không làm gì.
     * Gọi trực tiếp trên PackageRepository để tránh circular dependency với PackageServiceImpl.
     */
    private void revertPackageToDraft(Package pkg, String triggeredBy) {
        if (pkg.getPublishStatus() == PublishStatus.PUBLISHED) {
            pkg.setPublishStatus(PublishStatus.DRAFT);
            packageRepository.save(pkg);

            packageStatusPublisher.publish(new PackageStatusEvent(
                    pkg.getId(),
                    pkg.getName(),
                    pkg.getType() != null ? pkg.getType().getName().name() : null,
                    PublishStatus.DRAFT.name(),
                    triggeredBy,
                    "CONTENT_UPDATED"
            ));

            log.info("Package {} auto-reverted to DRAFT due to folder CUD by userId={}",
                    pkg.getId(), triggeredBy);
        }
    }

    // ── EXTENSION HOOKS ───────────────────────────────────────────────────────

    protected void validateCreateFolder(CreateFolderRequest request, String userId) {
    }

    protected void beforeSaveFolder(Folder folder, CreateFolderRequest request) {
    }

    protected void afterSaveFolder(Folder saved) {
    }

    protected void validateUpdateFolder(Folder entity, UpdateFolderRequest request, String userId) {
    }

    protected void beforeUpdateFolder(Folder entity, UpdateFolderRequest request) {
    }

    protected void afterUpdateFolder(Folder updated) {
    }

    protected void beforeDeleteFolder(Folder entity, String userId) {
    }

    protected void afterDeleteFolder(String folderId, String userId) {
    }
}
