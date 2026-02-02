package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.UpdateProgressRequest;
import com.lms.learningpath.dto.response.*;
import com.lms.learningpath.service.IProgressTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for user progress tracking on modules, steps, and learning paths.
 */
@RestController
@RequestMapping("/user/progress")
@RequiredArgsConstructor
public class UserProgressController {

    private final IProgressTrackingService progressService;

    // ============ Module Progress ============

    @PostMapping("/module/{moduleId}/start")
    public ResponseEntity<ApiResponse<ModuleProgressDto>> startModule(
            @PathVariable String moduleId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleProgressDto response = progressService.startModule(principal.userId(), moduleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/module/{moduleId}/update")
    public ResponseEntity<ApiResponse<ModuleProgressDto>> updateProgress(
            @PathVariable String moduleId,
            @RequestBody @Valid UpdateProgressRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleProgressDto response = progressService.updateProgress(
                principal.userId(), moduleId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/module/{moduleId}/complete")
    public ResponseEntity<ApiResponse<ModuleProgressDto>> completeModule(
            @PathVariable String moduleId,
            @RequestBody @Valid CompleteModuleRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleProgressDto response = progressService.completeModule(
                principal.userId(), moduleId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ============ Step Progress ============

    @GetMapping("/step/{stepId}")
    public ResponseEntity<ApiResponse<StepProgressResponse>> getStepProgress(
            @PathVariable String stepId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StepProgressResponse response = progressService.getStepProgress(principal.userId(), stepId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ============ Learning Path Progress ============

    @GetMapping("/learning-path/{learningPathId}")
    public ResponseEntity<ApiResponse<LearningPathProgressResponse>> getLearningPathProgress(
            @PathVariable String learningPathId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        LearningPathProgressResponse response = progressService.getLearningPathProgress(
                principal.userId(), learningPathId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/study-set/{studySetId}/learning-paths")
    public ResponseEntity<ApiResponse<List<LearningPathProgressResponse>>> getAllLearningPathProgress(
            @PathVariable String studySetId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        List<LearningPathProgressResponse> response = progressService.getAllLearningPathProgress(
                principal.userId(), studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
