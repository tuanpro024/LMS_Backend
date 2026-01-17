package com.lms.writing.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.writing.dto.request.CreateFolderRequest;
import com.lms.writing.dto.request.UpdateFolderRequest;
import com.lms.writing.dto.response.FolderResponse;
import com.lms.writing.entity.*;
import com.lms.writing.mapper.FolderMapper;
import com.lms.writing.repository.*;
import com.lms.writing.service.FolderService;
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
    private final SubjectRepository subjectRepository;
    private final SlotRepository slotRepository;
    private final StudySetRepository studySetRepository;
    private final FolderMapper folderMapper;

    @Override
    public FolderResponse createFolder(CreateFolderRequest request, String userId) {
        log.info("Creating folder for user: {}", userId);

        Folder folder = folderMapper.toEntity(request);
        folder.setUserId(userId);

        // Set optional parent relationships
        if (request.getPackageId() != null) {
            com.lms.writing.entity.Package packageEntity = packageRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));
            folder.setPackageEntity(packageEntity);
        }

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));
            folder.setSubject(subject);
        }

        if (request.getSlotId() != null) {
            Slot slot = slotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));
            folder.setSlot(slot);
        }

        Folder saved = folderRepository.save(folder);
        return folderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FolderResponse getFolderById(String id, String currentUserId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check access permission
        if (folder.isPrivate() && (currentUserId == null || !folder.getUserId().equals(currentUserId))) {
            throw new ApiException(ErrorCode.E240, "No permission to view this folder");
        }

        return folderMapper.toResponse(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getAllFolders(String currentUserId) {
        List<Folder> folders;

        if (currentUserId != null) {
            folders = folderRepository.findByUserIdOrIsPrivateFalse(currentUserId);
        } else {
            folders = folderRepository.findByIsPrivateFalse();
        }

        return folderMapper.toResponseList(folders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getFoldersByUserId(String userId, String currentUserId) {
        List<Folder> folders;

        if (currentUserId != null && userId.equals(currentUserId)) {
            folders = folderRepository.findByUserId(userId);
        } else {
            folders = folderRepository.findByUserId(userId).stream()
                    .filter(f -> !f.isPrivate())
                    .toList();
        }

        return folderMapper.toResponseList(folders);
    }

    @Override
    public FolderResponse updateFolder(String id, UpdateFolderRequest request, String userId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership
        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this folder");
        }

        folderMapper.updateEntity(folder, request);

        Folder updated = folderRepository.save(folder);
        return folderMapper.toResponse(updated);
    }

    @Override
    public void deleteFolder(String id, String userId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership
        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this folder");
        }

        folderRepository.delete(folder);
    }

    @Override
    public FolderResponse addStudySetToFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this folder");
        }

        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        folder.addStudySet(studySet);
        Folder updated = folderRepository.save(folder);

        return folderMapper.toResponse(updated);
    }

    @Override
    public FolderResponse removeStudySetFromFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this folder");
        }

        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        folder.removeStudySet(studySet);
        Folder updated = folderRepository.save(folder);

        return folderMapper.toResponse(updated);
    }

    @Override
    public void updateFolderPrivacy(String id, boolean isPrivate, String userId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this folder");
        }

        folder.setPrivate(isPrivate);
        folderRepository.save(folder);
    }
}
