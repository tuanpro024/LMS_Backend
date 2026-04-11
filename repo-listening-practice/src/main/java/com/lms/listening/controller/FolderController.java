package com.lms.listening.controller;

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
 * Thin controller that delegates to FolderApiDelegate from content-common
 */
@RestController
@RequestMapping("/api/listening-practice/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderApiDelegate delegate;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @RequestBody @Valid CreateFolderRequest request,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        FolderResponse response = delegate.createFolder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolderById(@PathVariable String id) {
        FolderResponse response = delegate.getFolderById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFoldersByPackage(
            @RequestParam String packageId) {
        List<FolderResponse> response = delegate.getFoldersByPackageId(packageId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            @PathVariable String id,
            @RequestBody @Valid UpdateFolderRequest request,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        FolderResponse response = delegate.updateFolder(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable String id,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        delegate.deleteFolder(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{folderId}/study-sets/{studySetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
    public ResponseEntity<ApiResponse<FolderResponse>> addStudySetToFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        FolderResponse response = delegate.addStudySetToFolder(folderId, studySetId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{folderId}/study-sets/{studySetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
    public ResponseEntity<ApiResponse<FolderResponse>> removeStudySetFromFolder(
            @PathVariable String folderId,
            @PathVariable String studySetId,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        FolderResponse response = delegate.removeStudySetFromFolder(folderId, studySetId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
