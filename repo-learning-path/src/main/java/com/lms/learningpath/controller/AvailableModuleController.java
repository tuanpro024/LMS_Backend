package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.learningpath.dto.response.AvailableModuleResponse;
import com.lms.learningpath.service.IAvailableModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for browsing available modules from other microservices.
 * Used by Admin/Teacher when selecting modules to add to steps.
 * Only accessible to Admin and Teacher roles.
 */
@RestController
@RequestMapping("/admin/available-modules")
@RequiredArgsConstructor
public class AvailableModuleController {

    private final IAvailableModuleService availableModuleService;

    /**
     * Get all available modules from all repos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAllAvailableModules(
            @RequestParam(required = false) String q) {

        List<AvailableModuleResponse> response = availableModuleService.getAllAvailableModules(q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get available flashcard study sets
     */
    @GetMapping("/flashcard")
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAvailableFlashcardSets(
            @RequestParam(required = false) String q) {

        List<AvailableModuleResponse> response = availableModuleService.getAvailableFlashcardSets(q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get available writing study sets
     */
    @GetMapping("/writing")
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAvailableWritingSets(
            @RequestParam(required = false) String q) {

        List<AvailableModuleResponse> response = availableModuleService.getAvailableWritingSets(q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get available kanji-origin study sets
     */
    @GetMapping("/kanji")
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAvailableKanjiSets(
            @RequestParam(required = false) String q) {

        List<AvailableModuleResponse> response = availableModuleService.getAvailableKanjiSets(q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
