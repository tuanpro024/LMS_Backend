package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.CreateModuleRequest;
import com.lms.learningpath.dto.request.UpdateModuleRequest;
import com.lms.learningpath.dto.response.ModuleCompleteResponse;
import com.lms.learningpath.dto.response.ModuleResponse;
import com.lms.learningpath.service.LearningModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
@Slf4j
public class LearningModuleController {

    private final LearningModuleService moduleService;

    /**
     * Create a new learning module (Admin/Teacher only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ModuleResponse>> createModule(
            @RequestBody @Valid CreateModuleRequest request,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleResponse response = moduleService.createModule(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get module by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleResponse>> getModuleById(@PathVariable String id) {
        ModuleResponse response = moduleService.getModuleById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Update module (Admin/Teacher only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ModuleResponse>> updateModule(
            @PathVariable String id,
            @RequestBody @Valid UpdateModuleRequest request,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleResponse response = moduleService.updateModule(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete module (Admin/Teacher only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteModule(
            @PathVariable String id,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        moduleService.deleteModule(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Start learning a module
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<ModuleCompleteResponse>> startModule(
            @PathVariable String id,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleCompleteResponse response = moduleService.startModule(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Complete a module
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ModuleCompleteResponse>> completeModule(
            @PathVariable String id,
            @RequestBody @Valid CompleteModuleRequest request,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleCompleteResponse response = moduleService.completeModule(id, principal.userId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}