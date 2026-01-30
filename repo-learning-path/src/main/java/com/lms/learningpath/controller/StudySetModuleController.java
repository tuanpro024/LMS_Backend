package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.request.CreateModuleRequest;
import com.lms.learningpath.dto.response.StudySetModuleResponse;
import com.lms.learningpath.entity.StudySetModule;
import com.lms.learningpath.repository.StudySetModuleRepository;
import com.lms.learningpath.service.IProgressTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/study-sets/{studySetId}/modules")
@RequiredArgsConstructor
public class StudySetModuleController {

    private final StudySetModuleRepository moduleRepo;
    private final IProgressTrackingService progressService;

    /**
     * Create a single module (Admin/Teacher only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<StudySetModule>> createModule(
            @PathVariable String studySetId,
            @RequestBody @Valid CreateModuleRequest request,
            Authentication authentication) {

        StudySetModule module = StudySetModule.builder()
                .studySetId(studySetId)
                .moduleType(request.getModuleType())
                .moduleOrder(request.getModuleOrder())
                .title(request.getTitle())
                .description(request.getDescription())
                .icon(request.getIcon())
                .color(request.getColor())
                .contentSetId(request.getContentSetId())
                .contentFolderId(request.getContentFolderId())
                .externalRefJson(request.getExternalRefJson())
                .estimatedMinutes(request.getEstimatedMinutes())
                .isRequired(request.getIsRequired())
                .isActive(true)
                .build();

        StudySetModule saved = moduleRepo.save(module);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    /**
     * Batch create modules (Admin/Teacher only)
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<StudySetModule>>> createModulesBatch(
            @PathVariable String studySetId,
            @RequestBody @Valid List<CreateModuleRequest> requests,
            Authentication authentication) {

        List<StudySetModule> modules = new ArrayList<>();
        for (CreateModuleRequest request : requests) {
            StudySetModule module = StudySetModule.builder()
                    .studySetId(studySetId)
                    .moduleType(request.getModuleType())
                    .moduleOrder(request.getModuleOrder())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .icon(request.getIcon())
                    .color(request.getColor())
                    .contentSetId(request.getContentSetId())
                    .contentFolderId(request.getContentFolderId())
                    .externalRefJson(request.getExternalRefJson())
                    .estimatedMinutes(request.getEstimatedMinutes())
                    .isRequired(request.getIsRequired())
                    .isActive(true)
                    .build();
            modules.add(module);
        }

        List<StudySetModule> saved = moduleRepo.saveAll(modules);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    /**
     * Get all modules for a StudySet with user progress
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudySetModuleResponse>>> getModules(
            @PathVariable String studySetId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        List<StudySetModuleResponse> modules = progressService.getModulesWithProgress(
                principal.userId(), studySetId);
        return ResponseEntity.ok(ApiResponse.ok(modules));
    }

    /**
     * Update a module (Admin/Teacher only)
     */
    @PutMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<StudySetModule>> updateModule(
            @PathVariable String studySetId,
            @PathVariable String moduleId,
            @RequestBody @Valid CreateModuleRequest request,
            Authentication authentication) {

        StudySetModule module = moduleRepo.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        module.setModuleType(request.getModuleType());
        module.setModuleOrder(request.getModuleOrder());
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setIcon(request.getIcon());
        module.setColor(request.getColor());
        module.setContentSetId(request.getContentSetId());
        module.setContentFolderId(request.getContentFolderId());
        module.setExternalRefJson(request.getExternalRefJson());
        module.setEstimatedMinutes(request.getEstimatedMinutes());
        module.setIsRequired(request.getIsRequired());

        StudySetModule updated = moduleRepo.save(module);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    /**
     * Delete a module (Admin/Teacher only)
     */
    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteModule(
            @PathVariable String studySetId,
            @PathVariable String moduleId,
            Authentication authentication) {

        moduleRepo.deleteById(moduleId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
