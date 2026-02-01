package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.request.CreateLearningPathRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.request.UpdateLearningPathRequest;
import com.lms.learningpath.dto.response.LearningPathResponse;
import com.lms.learningpath.service.ILearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing Learning Paths within StudySets.
 * Only Admin and Teacher can create/modify learning paths.
 */
@RestController
@RequestMapping("/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final ILearningPathService learningPathService;

    /**
     * Create a new learning path (Admin/Teacher only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<LearningPathResponse>> createLearningPath(
            @RequestBody @Valid CreateLearningPathRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        LearningPathResponse response = learningPathService.createLearningPath(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get learning path by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LearningPathResponse>> getLearningPathById(
            @PathVariable String id,
            Authentication authentication) {

        if (authentication != null) {
            AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
            LearningPathResponse response = learningPathService.getLearningPathWithProgress(id, principal.userId());
            return ResponseEntity.ok(ApiResponse.ok(response));
        }

        LearningPathResponse response = learningPathService.getLearningPathById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all learning paths for a study set
     */
    @GetMapping("/study-set/{studySetId}")
    public ResponseEntity<ApiResponse<List<LearningPathResponse>>> getLearningPathsByStudySet(
            @PathVariable String studySetId,
            Authentication authentication) {

        if (authentication != null) {
            AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
            List<LearningPathResponse> response = learningPathService
                    .getLearningPathsByStudySetIdWithProgress(studySetId, principal.userId());
            return ResponseEntity.ok(ApiResponse.ok(response));
        }

        List<LearningPathResponse> response = learningPathService.getLearningPathsByStudySetId(studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Update a learning path (Admin/Teacher only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<LearningPathResponse>> updateLearningPath(
            @PathVariable String id,
            @RequestBody @Valid UpdateLearningPathRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        LearningPathResponse response = learningPathService.updateLearningPath(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete a learning path (Admin/Teacher only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteLearningPath(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        learningPathService.deleteLearningPath(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
