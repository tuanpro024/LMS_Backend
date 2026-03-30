package com.lms.content.common.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.dto.request.CreateFolderRequest;
import com.lms.content.common.dto.request.UpdateFolderRequest;
import com.lms.content.common.dto.response.FolderResponse;
import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.StudySet;
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

    @Override
    public FolderResponse createFolder(CreateFolderRequest request, String userId) {
        log.info("Creating folder for user: {}", userId);

        // Hook: validate
        validateCreateFolder(request, userId);

        Folder folder = folderMapper.toEntity(request);
        folder.setUserId(userId);

        // Set package if provided
        if (request.getPackageId() != null) {
            Package packageEntity = packageRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));
            folder.setPackageEntity(packageEntity);
        }

        // Hook: before save
        beforeSaveFolder(folder, request);

        Folder saved = folderRepository.save(folder);

        // Hook: after save
        afterSaveFolder(saved);

        return folderMapper.toResponse(saved);
    }

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

    @Override
    public FolderResponse updateFolder(String id, UpdateFolderRequest request, String userId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership - DISABLED
        // if (!folder.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to modify this
        // folder");
        // }

        // Hook: validate update
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

        // Hook: before update
        beforeUpdateFolder(folder, request);

        Folder updated = folderRepository.save(folder);

        // Hook: after update
        afterUpdateFolder(updated);

        return folderMapper.toResponse(updated);
    }

    @Override
    public void deleteFolder(String id, String userId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership - DISABLED
        // if (!folder.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to delete this
        // folder");
        // }

        // Hook: before delete
        beforeDeleteFolder(folder, userId);

        // Note: Folder deletion should NOT delete StudySets (Many-to-Many).
        // JPA will automatically handle the removal of rows in the join table because
        // Folder owns the relationship.

        folderRepository.delete(folder);

        // Hook: after delete
        afterDeleteFolder(id, userId);
    }

    @Override
    public FolderResponse addStudySetToFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership - DISABLED
        // if (!folder.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to modify this
        // folder");
        // }

        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        folder.addStudySet(studySet);
        Folder updated = folderRepository.save(folder);

        return folderMapper.toResponse(updated);
    }

    @Override
    public FolderResponse removeStudySetFromFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership - DISABLED
        // if (!folder.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to modify this
        // folder");
        // }

        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        folder.removeStudySet(studySet);
        Folder updated = folderRepository.save(folder);

        return folderMapper.toResponse(updated);
    }

    @Override
    public void updateFolderPrivacy(String folderId, boolean isPrivate, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership - DISABLED
        // if (!folder.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to modify this
        // folder");
        // }

        folder.setPrivate(isPrivate);
        folderRepository.save(folder);
    }

    // ========== EXTENSION HOOKS ==========

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
