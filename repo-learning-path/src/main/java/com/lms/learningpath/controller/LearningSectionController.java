package com.lms.learningpath.controller;

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

/**
 * Controller for Learning Section management (using Folder entity from
 * repo-content-common).
 * Delegates to FolderApiDelegate for CRUD operations.
 */
@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class LearningSectionController {

    private final FolderApiDelegate folderDelegate;

    /**
     * Create a new learning section
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FolderResponse>> createSection(
            @RequestBody @Valid CreateFolderRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderDelegate.createFolder(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get section by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getSection(@PathVariable String id) {
        FolderResponse response = folderDelegate.getFolderById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all sections for a learning path
     */
    @GetMapping("/learning-path/{learningPathId}")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getSectionsByLearningPath(
            @PathVariable String learningPathId) {
        List<FolderResponse> response = folderDelegate.getFoldersByPackageId(learningPathId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Update section
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> updateSection(
            @PathVariable String id,
            @RequestBody @Valid UpdateFolderRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderDelegate.updateFolder(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete section
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        folderDelegate.deleteFolder(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
