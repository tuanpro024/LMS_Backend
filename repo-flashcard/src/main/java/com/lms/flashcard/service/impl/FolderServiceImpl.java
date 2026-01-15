package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.CreateFolderRequest;
import com.lms.flashcard.dto.response.FolderResponse;
import com.lms.flashcard.entity.Folder;
import com.lms.flashcard.entity.StudySet;
import com.lms.flashcard.mapper.FolderMapper;
import com.lms.flashcard.repository.FolderRepository;
import com.lms.flashcard.repository.PackageRepository;
import com.lms.flashcard.repository.StudySetRepository;
import com.lms.flashcard.service.FolderService;
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
    private final StudySetRepository studySetRepository;
    private final PackageRepository packageRepository;
    private final FolderMapper folderMapper;

    @Override
    public FolderResponse createFolder(CreateFolderRequest request, String userId) {
        log.info("Creating folder for user: {}", userId);

        Folder folder = folderMapper.toEntity(request);
        folder.setUserId(userId);

        // Set package if provided
        if (request.getPackageId() != null) {
            com.lms.flashcard.entity.Package packageEntity = packageRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

            // Check if user owns the package
            if (!packageEntity.getUserId().equals(userId)) {
                throw new ApiException(ErrorCode.E240, "No permission to assign folder to this package");
            }

            folder.setPackageEntity(packageEntity);
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
        if (folder.isPrivate() && !folder.getUserId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.E240, "No permission to view this folder");
        }

        return folderMapper.toResponse(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getAllPublicFolders() {
        List<Folder> folders = folderRepository.findByIsPrivateFalse();
        return folderMapper.toResponseList(folders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getAccessibleFolders(String currentUserId) {
        if (currentUserId == null) {
            // Not authenticated - only return public folders
            return getAllPublicFolders();
        }

        // Authenticated - return public folders + user's private folders
        List<Folder> folders = folderRepository.findAll().stream()
                .filter(f -> !f.isPrivate() || f.getUserId().equals(currentUserId))
                .toList();

        return folderMapper.toResponseList(folders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getFoldersByUserId(String userId, String currentUserId) {
        List<Folder> folders;

        if (userId.equals(currentUserId)) {
            // User can see all their own folders
            folders = folderRepository.findByUserId(userId);
        } else {
            // Others can only see public folders
            folders = folderRepository.findByUserId(userId).stream()
                    .filter(f -> !f.isPrivate())
                    .toList();
        }

        return folderMapper.toResponseList(folders);
    }

    @Override
    public FolderResponse addStudySetToFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership
        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this folder");
        }

        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        // Check if user has access to the study set
        if (studySet.isPrivate() && !studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add this study set");
        }

        folder.addStudySet(studySet);
        Folder updated = folderRepository.save(folder);

        return folderMapper.toResponse(updated);
    }

    @Override
    public FolderResponse removeStudySetFromFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership
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
    public FolderResponse updateFolderPrivacy(String folderId, boolean isPrivate, String userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check if user owns the folder
        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this folder");
        }

        folder.setPrivate(isPrivate);
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
}
