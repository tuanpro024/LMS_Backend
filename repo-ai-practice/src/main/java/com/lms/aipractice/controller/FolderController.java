package com.lms.aipractice.controller;

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

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderApiDelegate delegate;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<FolderResponse>> create(
            @RequestBody @Valid CreateFolderRequest request, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(delegate.createFolder(request, p.userId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(delegate.getFolderById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(delegate.getAllFolders()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<FolderResponse>> update(
            @PathVariable String id,
            @RequestBody @Valid UpdateFolderRequest request,
            Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.updateFolder(id, request, p.userId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        delegate.deleteFolder(id, p.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{folderId}/study-sets/{studySetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<FolderResponse>> addStudySet(
            @PathVariable String folderId, @PathVariable String studySetId, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.addStudySetToFolder(folderId, studySetId, p.userId())));
    }

    @DeleteMapping("/{folderId}/study-sets/{studySetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<FolderResponse>> removeStudySet(
            @PathVariable String folderId, @PathVariable String studySetId, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.removeStudySetFromFolder(folderId, studySetId, p.userId())));
    }
}
