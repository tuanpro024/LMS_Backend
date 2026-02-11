package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.content.common.dto.request.CreateStudySetRequest;
import com.lms.content.common.dto.request.UpdateStudySetRequest;
import com.lms.content.common.dto.response.StudySetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing local StudySets (HSK1, HSK2, etc.)
 * These are the learning sets within the learning path.
 */
@RestController
@RequestMapping("/study-sets")
@RequiredArgsConstructor
public class StudySetController {

    private final StudySetApiDelegate studySetDelegate;

    /**
     * Create a new study set
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<StudySetResponse>> createStudySet(
            @RequestBody @Valid CreateStudySetRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StudySetResponse response = studySetDelegate.createStudySet(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get study set by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySetResponse>> getStudySetById(@PathVariable String id) {
        StudySetResponse response = studySetDelegate.getStudySetById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all study sets
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getAllStudySets(
            @RequestParam(required = false) String q) {

        List<StudySetResponse> response = q != null && !q.isBlank()
                ? studySetDelegate.searchStudySets(q)
                : studySetDelegate.getAllStudySets();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get study sets by folder ID
     */
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getStudySetsByFolder(
            @PathVariable String folderId) {

        List<StudySetResponse> response = studySetDelegate.getStudySetsByFolderId(folderId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get my study sets (created by current user)
     */
    @GetMapping("/my-sets")
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getMyStudySets(
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        List<StudySetResponse> response = studySetDelegate.getStudySetsByUserId(principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Update study set
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<StudySetResponse>> updateStudySet(
            @PathVariable String id,
            @RequestBody @Valid UpdateStudySetRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StudySetResponse response = studySetDelegate.updateStudySet(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete study set
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudySet(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        studySetDelegate.deleteStudySet(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}