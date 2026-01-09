package com.lms.flashcard.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.flashcard.dto.request.CreateFolderRequest;
import com.lms.flashcard.dto.response.FolderResponse;
import com.lms.flashcard.service.FolderService;
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

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @RequestBody @Valid CreateFolderRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderService.createFolder(request, principal.userId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolderById(
            @PathVariable String id,
            Authentication authentication) {

        String userId = authentication != null && authentication.getPrincipal() instanceof AuthPrincipal
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;

        FolderResponse response = folderService.getFolderById(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFolders(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false, defaultValue = "false") boolean rootOnly,
            Authentication authentication) {

        String currentUserId = authentication != null && authentication.getPrincipal() instanceof AuthPrincipal
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;

        List<FolderResponse> response;

        if (rootOnly && currentUserId != null) {
            response = folderService.getRootFoldersByUserId(currentUserId);
        } else if (userId != null) {
            response = folderService.getFoldersByUserId(userId, currentUserId);
        } else {
            response = folderService.getAllPublicFolders();
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{folderId}/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<FolderResponse>> addStudySetToFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderService.addStudySetToFolder(folderId, studySetId, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{folderId}/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<FolderResponse>> removeStudySetFromFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        FolderResponse response = folderService.removeStudySetFromFolder(folderId, studySetId, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        folderService.deleteFolder(id, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
