package com.lms.content.common.delegate.api;

import com.lms.content.common.dto.request.CreateFolderRequest;
import com.lms.content.common.dto.request.UpdateFolderRequest;
import com.lms.content.common.dto.response.FolderResponse;
import com.lms.content.common.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FolderApiDelegate {

    private final FolderService folderService;

    public FolderResponse createFolder(CreateFolderRequest request, String userId) {
        return folderService.createFolder(request, userId);
    }

    public FolderResponse getFolderById(String id) {
        return folderService.getFolderById(id);
    }

    public List<FolderResponse> getFoldersByUserId(String userId) {
        return folderService.getFoldersByUserId(userId);
    }

    public List<FolderResponse> getFoldersByPackageId(String packageId) {
        return folderService.getFoldersByPackageId(packageId);
    }

    public List<FolderResponse> getFoldersBySubjectId(String subjectId) {
        return folderService.getFoldersBySubjectId(subjectId);
    }

    public List<FolderResponse> getFoldersBySlotId(String slotId) {
        return folderService.getFoldersBySlotId(slotId);
    }

    public List<FolderResponse> getAllFolders() {
        return folderService.getAllFolders();
    }

    public List<FolderResponse> getFoldersByStudySetId(String studySetId) {
        return folderService.getFoldersByStudySetId(studySetId);
    }

    public FolderResponse updateFolder(String id, UpdateFolderRequest request, String userId) {
        return folderService.updateFolder(id, request, userId);
    }

    public void deleteFolder(String id, String userId) {
        folderService.deleteFolder(id, userId);
    }

    public FolderResponse addStudySetToFolder(String folderId, String studySetId, String userId) {
        return folderService.addStudySetToFolder(folderId, studySetId, userId);
    }

    public FolderResponse removeStudySetFromFolder(String folderId, String studySetId, String userId) {
        return folderService.removeStudySetFromFolder(folderId, studySetId, userId);
    }

    public void updateFolderPrivacy(String folderId, boolean isPrivate, String userId) {
        folderService.updateFolderPrivacy(folderId, isPrivate, userId);
    }
}
