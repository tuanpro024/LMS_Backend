package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.learningpath.dto.external.StudySetDto;
import com.lms.learningpath.entity.enums.ModuleType;
import com.lms.learningpath.service.ModuleIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for admin/teacher interface to browse and select StudySets from
 * different modules.
 * Provides APIs to list available StudySets across all modules for adding to
 * learning sections.
 */
@RestController
@RequestMapping("/admin/study-sets")
@RequiredArgsConstructor
public class StudySetController {

    private final ModuleIntegrationService moduleIntegrationService;
    private final StudySetApiDelegate studySetDelegate;

    /**
     * Get all available StudySets grouped by module type
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Map<String, List<StudySetDto>>>> getAllAvailableStudySets(
            Authentication authentication) {

        Map<String, List<StudySetDto>> studySetsByModule = new HashMap<>();

        // Fetch from each module
        for (ModuleType moduleType : ModuleType.values()) {
            try {
                List<StudySetDto> studySets = moduleIntegrationService.getAllStudySets(moduleType);
                studySetsByModule.put(moduleType.name(), studySets);
            } catch (Exception e) {
                studySetsByModule.put(moduleType.name(), new ArrayList<>());
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(studySetsByModule));
    }

    /**
     * Get StudySets from a specific module
     */
    @GetMapping("/module/{moduleType}")
    public ResponseEntity<ApiResponse<List<StudySetDto>>> getStudySetsByModule(
            @PathVariable ModuleType moduleType,
            @RequestParam(required = false) String q,
            Authentication authentication) {

        List<StudySetDto> studySets = q != null && !q.isBlank()
                ? moduleIntegrationService.searchStudySets(moduleType, q)
                : moduleIntegrationService.getAllStudySets(moduleType);

        return ResponseEntity.ok(ApiResponse.ok(studySets));
    }

    /**
     * Get local StudySets (from this service's own study sets if any)
     */
    @GetMapping("/local")
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getLocalStudySets(
            @RequestParam(required = false) String q,
            Authentication authentication) {

        List<StudySetResponse> studySets = q != null && !q.isBlank()
                ? studySetDelegate.searchStudySets(q)
                : studySetDelegate.getAllStudySets();

        return ResponseEntity.ok(ApiResponse.ok(studySets));
    }

    /**
     * Get StudySet details from a specific module
     */
    @GetMapping("/module/{moduleType}/{studySetId}")
    public ResponseEntity<ApiResponse<StudySetDto>> getStudySetDetails(
            @PathVariable ModuleType moduleType,
            @PathVariable String studySetId,
            Authentication authentication) {

        StudySetDto studySet = moduleIntegrationService.getStudySet(moduleType, studySetId)
                .orElseThrow(() -> new RuntimeException("StudySet not found"));

        return ResponseEntity.ok(ApiResponse.ok(studySet));
    }
}
