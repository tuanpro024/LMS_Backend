package com.lms.writing.service;

import com.lms.writing.dto.request.CreateFolderRequest;
import com.lms.writing.dto.request.UpdateFolderRequest;
import com.lms.writing.dto.response.FolderResponse;

import java.util.List;

public interface FolderService {

    FolderResponse createFolder(CreateFolderRequest request, String userId);

    FolderResponse getFolderById(String id, String currentUserId);

    List<FolderResponse> getAllFolders(String currentUserId);

    List<FolderResponse> getFoldersByUserId(String userId, String currentUserId);

    FolderResponse updateFolder(String id, UpdateFolderRequest request, String userId);

    void deleteFolder(String id, String userId);

    FolderResponse addStudySetToFolder(String folderId, String studySetId, String userId);

    FolderResponse removeStudySetFromFolder(String folderId, String studySetId, String userId);

    void updateFolderPrivacy(String id, boolean isPrivate, String userId);
}
