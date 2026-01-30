package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.learningpath.dto.request.CreateUnlockRuleRequest;
import com.lms.learningpath.dto.request.UpdateUnlockRuleRequest;
import com.lms.learningpath.dto.response.UnlockRuleResponse;
import com.lms.learningpath.service.IUnlockRuleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing StudySet unlock rules.
 * Allows admin/teacher to define learning path progression logic.
 */
@RestController
@RequestMapping("/admin/unlock-rules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
public class UnlockRuleController {

    private final IUnlockRuleManagementService unlockRuleService;

    /**
     * Create a new unlock rule.
     * Defines which StudySet must be completed before another can be unlocked.
     *
     * @param request the create request
     * @return the created unlock rule
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UnlockRuleResponse>> createUnlockRule(
            @RequestBody @Valid CreateUnlockRuleRequest request) {

        UnlockRuleResponse response = unlockRuleService.createUnlockRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get unlock rule by ID.
     *
     * @param id the unlock rule ID
     * @return the unlock rule
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnlockRuleResponse>> getUnlockRuleById(@PathVariable String id) {
        UnlockRuleResponse response = unlockRuleService.getUnlockRuleById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all unlock rules for a specific StudySet.
     * Shows what requirements must be met to unlock this StudySet.
     *
     * @param studySetId the StudySet ID that needs to be unlocked
     * @return list of unlock rules
     */
    @GetMapping("/study-set/{studySetId}")
    public ResponseEntity<ApiResponse<List<UnlockRuleResponse>>> getUnlockRulesByStudySet(
            @PathVariable String studySetId) {

        List<UnlockRuleResponse> response = unlockRuleService.getUnlockRulesByStudySet(studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Find which StudySets will unlock after completing the given StudySet.
     * Shows the learning path progression from this StudySet.
     *
     * @param studySetId the completed StudySet ID
     * @return list of unlock rules where this StudySet is required
     */
    @GetMapping("/unlocks-after/{studySetId}")
    public ResponseEntity<ApiResponse<List<UnlockRuleResponse>>> getUnlocksAfterStudySet(
            @PathVariable String studySetId) {

        List<UnlockRuleResponse> response = unlockRuleService.getUnlockRulesRequiringStudySet(studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Update an existing unlock rule.
     *
     * @param id      the unlock rule ID
     * @param request the update request
     * @return the updated unlock rule
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UnlockRuleResponse>> updateUnlockRule(
            @PathVariable String id,
            @RequestBody @Valid UpdateUnlockRuleRequest request) {

        UnlockRuleResponse response = unlockRuleService.updateUnlockRule(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete an unlock rule (soft delete).
     * Sets the rule's isActive status to false.
     *
     * @param id the unlock rule ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUnlockRule(@PathVariable String id) {
        unlockRuleService.deleteUnlockRule(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
