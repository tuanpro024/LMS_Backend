package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.content.common.delegate.api.FolderApiDelegate;
import com.lms.content.common.dto.request.CreateFolderRequest;
import com.lms.content.common.dto.request.UpdateFolderRequest;
import com.lms.content.common.dto.response.FolderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing Folders within Learning Paths.
 * Folders group StudySets together (e.g., "Giáo Trình Chuẩn HSK", "Giáo Trình
 * Hán Ngữ")
 */
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderApiDelegate folderDelegate;

    /**
     * Create a new folder
     */
    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @RequestBody @Valid CreateFolderRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderDelegate.createFolder(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get folder by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolderById(@PathVariable String id) {
        FolderResponse response = folderDelegate.getFolderById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all folders by package ID (learning path)
     */
    @GetMapping("/package/{packageId}")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFoldersByPackage(
            @PathVariable String packageId) {

        List<FolderResponse> response = folderDelegate.getFoldersByPackageId(packageId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all folders by user
     */
    @GetMapping("/my-folders")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getMyFolders(
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        List<FolderResponse> response = folderDelegate.getFoldersByUserId(principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Update folder
     */
    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            @PathVariable String id,
            @RequestBody @Valid UpdateFolderRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderDelegate.updateFolder(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete folder
     */
    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        folderDelegate.deleteFolder(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Add a StudySet to a Folder
     */
    @PostMapping("/{folderId}/study-sets/{studySetId}")
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<FolderResponse>> addStudySetToFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderDelegate.addStudySetToFolder(folderId, studySetId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Remove a StudySet from a Folder
     */
    @DeleteMapping("/{folderId}/study-sets/{studySetId}")
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<FolderResponse>> removeStudySetFromFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderDelegate.removeStudySetFromFolder(folderId, studySetId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}