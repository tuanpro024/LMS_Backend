package com.lms.videocourse.controller;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing Folders within Video Course packages.
 * A Folder groups StudySets (e.g. "HSK 1", "HSK 2").
 * Delegates to FolderApiDelegate from repo-content-common.
 */
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderApiDelegate folderDelegate;

    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @RequestBody @Valid CreateFolderRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderDelegate.createFolder(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolderById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(folderDelegate.getFolderById(id)));
    }

    @GetMapping("/package/{packageId}")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFoldersByPackage(
            @PathVariable String packageId) {
        return ResponseEntity.ok(ApiResponse.ok(folderDelegate.getFoldersByPackageId(packageId)));
    }

    @GetMapping("/my-folders")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getMyFolders(
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(folderDelegate.getFoldersByUserId(principal.userId())));
    }

    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            @PathVariable String id,
            @RequestBody @Valid UpdateFolderRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(folderDelegate.updateFolder(id, request, principal.userId())));
    }

    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        folderDelegate.deleteFolder(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{folderId}/study-sets/{studySetId}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<FolderResponse>> addStudySetToFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                folderDelegate.addStudySetToFolder(folderId, studySetId, principal.userId())));
    }

    @DeleteMapping("/{folderId}/study-sets/{studySetId}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<FolderResponse>> removeStudySetFromFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                folderDelegate.removeStudySetFromFolder(folderId, studySetId, principal.userId())));
    }
}
