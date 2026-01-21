package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.CreateFolderRequest;
import com.lms.flashcard.dto.request.UpdateFolderRequest;
import com.lms.flashcard.dto.response.FolderResponse;
import com.lms.flashcard.dto.response.StudySetResponse;
import com.lms.flashcard.entity.Folder;
import com.lms.flashcard.entity.StudySet;
import com.lms.flashcard.mapper.FolderMapper;
import com.lms.flashcard.mapper.StudySetMapper;
import com.lms.flashcard.repository.FolderRepository;
import com.lms.flashcard.repository.StudySetRepository;
import com.lms.flashcard.repository.UserCardProgressRepository;
import com.lms.flashcard.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final StudySetRepository studySetRepository;
    private final FolderMapper folderMapper;
    private final StudySetMapper studySetMapper;
    private final UserCardProgressRepository userCardProgressRepository;

    @Override
    public FolderResponse createFolder(CreateFolderRequest request, String userId) {
        log.info("Creating folder for user: {}", userId);

        Folder folder = folderMapper.toEntity(request);
        folder.setUserId(userId);

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

        // Map folder response with user-specific progress
        return mapFolderWithUserProgress(folder, currentUserId);
    }
    
    /**
     * Helper method to map folder with user-specific study set progress
     */
    private FolderResponse mapFolderWithUserProgress(Folder folder, String userId) {
        FolderResponse response = folderMapper.toResponse(folder);
        
        // Calculate progress for each study set based on current user
        if (folder.getStudySets() != null && !folder.getStudySets().isEmpty()) {
            List<StudySetResponse> studySetResponses = folder.getStudySets().stream()
                    .map(studySet -> {
                        StudySetResponse studySetResponse = studySetMapper.toResponse(studySet);
                        calculateAndSetProgress(studySetResponse, studySet, userId);
                        return studySetResponse;
                    })
                    .collect(Collectors.toList());
            response.setStudySets(studySetResponses);
        }
        
        return response;
    }
    
    /**
     * Calculate progress for a study set based on user's card progress
     */
    private void calculateAndSetProgress(StudySetResponse response, StudySet studySet, String userId) {
        if (studySet.getCards() == null || studySet.getCards().isEmpty()) {
            response.setProgress(0);
            return;
        }
        
        if (userId == null) {
            response.setProgress(0);
            return;
        }
        
        long total = studySet.getCards().size();
        long learned = userCardProgressRepository.countByUserIdAndStudySetIdAndStatus(
                userId, 
                studySet.getId(), 
                com.lms.flashcard.entity.enums.CardStatus.LEARNED
        );
        
        double progress = ((double) learned / total) * 100;
        response.setProgress(progress);
    }

    @Override
    public FolderResponse updateFolder(String id, UpdateFolderRequest request, String userId) {
        log.info("Updating folder {} for user: {}", id, userId);

        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership
        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this folder");
        }

        // Update fields if provided
        if (request.getName() != null) {
            folder.setName(request.getName());
        }
        if (request.getDescription() != null) {
            folder.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            folder.setColor(request.getColor());
        }

        Folder updated = folderRepository.save(folder);
        return folderMapper.toResponse(updated);
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
