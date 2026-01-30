package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.request.AddModuleToSectionRequest;
import com.lms.learningpath.dto.response.SectionModuleResponse;
import com.lms.learningpath.entity.SectionModule;
import com.lms.learningpath.repository.SectionModuleRepository;
import com.lms.learningpath.service.ProgressTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing modules within learning sections.
 * Handles adding/removing modules and reordering.
 */
@RestController
@RequestMapping("/sections/{sectionId}/modules")
@RequiredArgsConstructor
public class SectionModuleController {

    private final SectionModuleRepository moduleRepository;
    private final ProgressTrackingService progressTrackingService;

    /**
     * Add a module to a section
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SectionModule>> addModule(
            @PathVariable String sectionId,
            @RequestBody @Valid AddModuleToSectionRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        SectionModule module = SectionModule.builder()
                .folderId(sectionId)
                .moduleType(request.getModuleType())
                .studySetId(request.getStudySetId())
                .moduleOrder(request.getModuleOrder())
                .displayTitle(request.getDisplayTitle())
                .isRequired(request.getIsRequired())
                .moduleDescription(request.getModuleDescription())
                .build();

        SectionModule saved = moduleRepository.save(module);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    /**
     * Get all modules in a section (with enriched StudySet data)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SectionModuleResponse>>> getModules(
            @PathVariable String sectionId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        List<SectionModuleResponse> modules = progressTrackingService
                .getModulesWithProgress(principal.userId(), sectionId);
        return ResponseEntity.ok(ApiResponse.ok(modules));
    }

    /**
     * Delete a module from a section
     */
    @DeleteMapping("/{moduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteModule(
            @PathVariable String sectionId,
            @PathVariable String moduleId,
            Authentication authentication) {

        moduleRepository.deleteById(moduleId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Update module order and details
     */
    @PutMapping("/{moduleId}")
    public ResponseEntity<ApiResponse<SectionModule>> updateModule(
            @PathVariable String sectionId,
            @PathVariable String moduleId,
            @RequestBody @Valid AddModuleToSectionRequest request,
            Authentication authentication) {

        SectionModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        module.setModuleOrder(request.getModuleOrder());
        module.setDisplayTitle(request.getDisplayTitle());
        module.setIsRequired(request.getIsRequired());
        module.setModuleDescription(request.getModuleDescription());

        SectionModule updated = moduleRepository.save(module);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }
}
