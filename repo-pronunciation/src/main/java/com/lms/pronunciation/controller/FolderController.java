package com.lms.pronunciation.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
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

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderApiDelegate delegate;

    @PostMapping
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @RequestBody @Valid CreateFolderRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = delegate.createFolder(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolderById(@PathVariable String id) {
        FolderResponse response = delegate.getFolderById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getAllFolders(
            @RequestParam(required = false) String userId) {
        if (userId != null) {
            List<FolderResponse> response = delegate.getFoldersByUserId(userId);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
        List<FolderResponse> response = delegate.getAllFolders();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/package/{packageId}")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFoldersByPackageId(
            @PathVariable String packageId) {
        List<FolderResponse> response = delegate.getFoldersByPackageId(packageId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            @PathVariable String id,
            @RequestBody @Valid UpdateFolderRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = delegate.updateFolder(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/privacy")
    public ResponseEntity<ApiResponse<Void>> updateFolderPrivacy(
            @PathVariable String id,
            @RequestParam boolean isPrivate,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        delegate.updateFolderPrivacy(id, isPrivate, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        delegate.deleteFolder(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{folderId}/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<FolderResponse>> addStudySetToFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = delegate.addStudySetToFolder(folderId, studySetId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{folderId}/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<FolderResponse>> removeStudySetFromFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = delegate.removeStudySetFromFolder(folderId, studySetId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
