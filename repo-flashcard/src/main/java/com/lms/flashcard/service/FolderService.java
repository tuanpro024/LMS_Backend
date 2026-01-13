package com.lms.flashcard.service;

import com.lms.flashcard.dto.request.CreateFolderRequest;
import com.lms.flashcard.dto.response.FolderResponse;

import java.util.List;

public interface FolderService {

    FolderResponse createFolder(CreateFolderRequest request, String userId);

    FolderResponse getFolderById(String id, String currentUserId);

    List<FolderResponse> getAllPublicFolders();

    List<FolderResponse> getAccessibleFolders(String currentUserId);

    List<FolderResponse> getFoldersByUserId(String userId, String currentUserId);

    FolderResponse addStudySetToFolder(String folderId, String studySetId, String userId);

    FolderResponse removeStudySetFromFolder(String folderId, String studySetId, String userId);

    FolderResponse updateFolderPrivacy(String folderId, boolean isPrivate, String userId);

    void deleteFolder(String id, String userId);
}
