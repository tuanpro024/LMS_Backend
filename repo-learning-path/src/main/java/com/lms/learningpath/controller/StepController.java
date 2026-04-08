package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.request.CreateStepRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.request.UpdateStepRequest;
import com.lms.learningpath.dto.response.StepResponse;
import com.lms.learningpath.service.IStepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing Steps within Learning Paths.
 * Only Admin and Teacher can create/modify steps.
 */
@RestController
@RequestMapping("/steps")
@RequiredArgsConstructor
public class StepController {

    private final IStepService stepService;

    /**
     * Create a new step (Admin/Teacher only)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StepResponse>> createStep(
            @RequestBody @Valid CreateStepRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StepResponse response = stepService.createStep(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get step by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StepResponse>> getStepById(
            @PathVariable String id,
            Authentication authentication) {

        if (authentication != null) {
            AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
            StepResponse response = stepService.getStepWithProgress(id, principal.userId());
            return ResponseEntity.ok(ApiResponse.ok(response));
        }

        StepResponse response = stepService.getStepById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all steps for a learning path
     */
    @GetMapping("/learning-path/{learningPathId}")
    public ResponseEntity<ApiResponse<List<StepResponse>>> getStepsByLearningPath(
            @PathVariable String learningPathId,
            Authentication authentication) {

        if (authentication != null) {
            AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
            List<StepResponse> response = stepService
                    .getStepsByLearningPathIdWithProgress(learningPathId, principal.userId());
            return ResponseEntity.ok(ApiResponse.ok(response));
        }

        List<StepResponse> response = stepService.getStepsByLearningPathId(learningPathId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Update a step (Admin/Teacher only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StepResponse>> updateStep(
            @PathVariable String id,
            @RequestBody @Valid UpdateStepRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StepResponse response = stepService.updateStep(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete a step (Admin/Teacher only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStep(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        stepService.deleteStep(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Reorder steps within a learning path (Admin/Teacher only)
     */
    @PutMapping("/learning-path/{learningPathId}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderSteps(
            @PathVariable String learningPathId,
            @RequestBody @Valid ReorderItemsRequest request) {

        stepService.reorderSteps(learningPathId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
