package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.request.AddModuleToStepRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.response.StepModuleResponse;
import com.lms.learningpath.service.IStepModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing practice modules within Steps.
 * Only Admin and Teacher can add/remove modules.
 */
@RestController
@RequestMapping("/step-modules")
@RequiredArgsConstructor
public class StepModuleController {

    private final IStepModuleService stepModuleService;

    /**
     * Add a module to a step (Admin/Teacher only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<StepModuleResponse>> addModuleToStep(
            @RequestBody @Valid AddModuleToStepRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StepModuleResponse response = stepModuleService.addModuleToStep(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get module by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StepModuleResponse>> getModuleById(@PathVariable String id) {
        StepModuleResponse response = stepModuleService.getModuleById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all modules for a step
     */
    @GetMapping("/step/{stepId}")
    public ResponseEntity<ApiResponse<List<StepModuleResponse>>> getModulesByStep(
            @PathVariable String stepId) {

        List<StepModuleResponse> response = stepModuleService.getModulesByStepId(stepId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Remove a module from a step (Admin/Teacher only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> removeModuleFromStep(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        stepModuleService.removeModuleFromStep(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Reorder modules within a step (Admin/Teacher only)
     */
    @PutMapping("/step/{stepId}/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> reorderModules(
            @PathVariable String stepId,
            @RequestBody @Valid ReorderItemsRequest request) {

        stepModuleService.reorderModules(stepId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
