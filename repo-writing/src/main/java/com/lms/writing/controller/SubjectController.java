package com.lms.writing.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.writing.dto.request.CreateSubjectRequest;
import com.lms.writing.dto.request.UpdateSubjectRequest;
import com.lms.writing.dto.response.SubjectResponse;
import com.lms.writing.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @RequestBody @Valid CreateSubjectRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SubjectResponse response = subjectService.createSubject(request, principal.userId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubject(@PathVariable String id) {
        SubjectResponse response = subjectService.getSubjectById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjects(
            @RequestParam(required = false) String packageId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        List<SubjectResponse> response;

        if (packageId != null) {
            response = subjectService.getSubjectsByPackageId(packageId);
        } else {
            response = subjectService.getAllSubjects(principal.userId());
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable String id,
            @RequestBody @Valid UpdateSubjectRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SubjectResponse response = subjectService.updateSubject(id, request, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        subjectService.deleteSubject(id, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{subjectId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> addFolderToSubject(
            @PathVariable String subjectId,
            @PathVariable String folderId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SubjectResponse response = subjectService.addFolderToSubject(subjectId, folderId, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{subjectId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> removeFolderFromSubject(
            @PathVariable String subjectId,
            @PathVariable String folderId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SubjectResponse response = subjectService.removeFolderFromSubject(subjectId, folderId, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
