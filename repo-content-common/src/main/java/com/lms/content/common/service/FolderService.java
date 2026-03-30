package com.lms.content.common.service;

import com.lms.content.common.dto.request.CreateFolderRequest;
import com.lms.content.common.dto.request.UpdateFolderRequest;
import com.lms.content.common.dto.response.FolderResponse;

import java.util.List;

public interface FolderService {

    FolderResponse createFolder(CreateFolderRequest request, String userId);

    FolderResponse getFolderById(String id);

    List<FolderResponse> getFoldersByUserId(String userId);

    List<FolderResponse> getFoldersByPackageId(String packageId);

    List<FolderResponse> getAllFolders();

    FolderResponse updateFolder(String id, UpdateFolderRequest request, String userId);

    void deleteFolder(String id, String userId);

    FolderResponse addStudySetToFolder(String folderId, String studySetId, String userId);

    FolderResponse removeStudySetFromFolder(String folderId, String studySetId, String userId);

    void updateFolderPrivacy(String folderId, boolean isPrivate, String userId);

    List<FolderResponse> getFoldersByStudySetId(String studySetId);
}
