package com.lms.writing.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.writing.dto.request.CreateFolderRequest;
import com.lms.writing.dto.request.UpdateFolderRequest;
import com.lms.writing.dto.response.FolderResponse;
import com.lms.writing.dto.response.StudySetResponse;
import com.lms.writing.entity.*;
import com.lms.writing.mapper.FolderMapper;
import com.lms.writing.mapper.WordMapper;
import com.lms.writing.repository.*;
import com.lms.writing.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
    private final WordMapper wordMapper;

    @Override
    public FolderResponse createFolder(CreateFolderRequest request, String userId) {
        log.info("Creating folder for user: {}", userId);

        Folder folder = folderMapper.toEntity(request);
        folder.setUserId(userId);

        // Set optional parent relationships

        if (request.getPackageId() != null) {
            com.lms.writing.entity.Package packageEntity = packageRepository
                    .findById(Objects.requireNonNull(request.getPackageId()))
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));
            folder.setPackageEntity(packageEntity);
        }

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(Objects.requireNonNull(request.getSubjectId()))
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));
            folder.setSubject(subject);
        }

        if (request.getSlotId() != null) {
            Slot slot = slotRepository.findById(Objects.requireNonNull(request.getSlotId()))
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));
            folder.setSlot(slot);
        }

        Folder saved = folderRepository.save(folder);
        return folderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FolderResponse getFolderById(String id, String currentUserId) {
        Folder folder = folderRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check access permission
        if (folder.isPrivate() && (currentUserId == null || !folder.getUserId().equals(currentUserId))) {
            throw new ApiException(ErrorCode.E240, "No permission to view this folder");
        }

        FolderResponse response = folderMapper.toResponse(folder);
        // Manually map words for each study set
        if (response.getStudySets() != null && folder.getStudySets() != null) {
            mapWordsToStudySets(response, folder);
        }
        return response;
    }

    private void mapWordsToStudySets(FolderResponse response, Folder folder) {
        for (int i = 0; i < response.getStudySets().size(); i++) {
            StudySetResponse setResponse = response.getStudySets().get(i);
            StudySet setEntity = folder.getStudySets().get(i);
            if (setEntity.getWords() != null) {
                setResponse.setWords(wordMapper.toResponseList(setEntity.getWords()));
            }
        }
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

        List<FolderResponse> responses = folderMapper.toResponseList(folders);
        for (int i = 0; i < responses.size(); i++) {
            mapWordsToStudySets(responses.get(i), folders.get(i));
        }
        return responses;
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

        List<FolderResponse> responses = folderMapper.toResponseList(folders);
        // Map words for each folder's study sets
        for (int i = 0; i < responses.size(); i++) {
            mapWordsToStudySets(responses.get(i), folders.get(i));
        }
        return responses;
    }

    @Override
    public FolderResponse updateFolder(String id, UpdateFolderRequest request, String userId) {
        Folder folder = folderRepository.findById(Objects.requireNonNull(id))
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
        Folder folder = folderRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership
        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this folder");
        }

        folderRepository.delete(folder);
    }

    @Override
    public FolderResponse addStudySetToFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(Objects.requireNonNull(folderId))
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this folder");
        }

        StudySet studySet = studySetRepository.findById(Objects.requireNonNull(studySetId))
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        folder.addStudySet(studySet);
        Folder updated = folderRepository.save(folder);

        return folderMapper.toResponse(updated);
    }

    @Override
    public FolderResponse removeStudySetFromFolder(String folderId, String studySetId, String userId) {
        Folder folder = folderRepository.findById(Objects.requireNonNull(folderId))
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
