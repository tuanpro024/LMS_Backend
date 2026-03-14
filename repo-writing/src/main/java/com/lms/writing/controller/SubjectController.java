package com.lms.writing.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.SubjectApiDelegate;
import com.lms.content.common.dto.request.CreateSubjectRequest;
import com.lms.content.common.dto.request.UpdateSubjectRequest;
import com.lms.content.common.dto.response.SubjectResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectApiDelegate delegate;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @RequestBody @Valid CreateSubjectRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SubjectResponse response = delegate.createSubject(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(@PathVariable String id) {
        SubjectResponse response = delegate.getSubjectById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubjects() {
        List<SubjectResponse> response = delegate.getAllSubjects();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/package/{packageId}")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjectsByPackageId(
            @PathVariable String packageId) {
        List<SubjectResponse> response = delegate.getSubjectsByPackageId(packageId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable String id,
            @RequestBody @Valid UpdateSubjectRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SubjectResponse response = delegate.updateSubject(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        delegate.deleteSubject(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{subjectId}/folders/{folderId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubjectResponse>> addFolderToSubject(
            @PathVariable String subjectId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SubjectResponse response = delegate.addFolderToSubject(subjectId, folderId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{subjectId}/folders/{folderId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubjectResponse>> removeFolderFromSubject(
            @PathVariable String subjectId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SubjectResponse response = delegate.removeFolderFromSubject(subjectId, folderId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
