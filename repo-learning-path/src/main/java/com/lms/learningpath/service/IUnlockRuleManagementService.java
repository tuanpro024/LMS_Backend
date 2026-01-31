package com.lms.learningpath.service;

import com.lms.learningpath.dto.request.CreateUnlockRuleRequest;
import com.lms.learningpath.dto.request.UpdateUnlockRuleRequest;
import com.lms.learningpath.dto.response.UnlockRuleResponse;

import java.util.List;

/**
 * Service interface for managing StudySet unlock rules (CRUD operations).
 */
public interface IUnlockRuleManagementService {

    /**
     * Create a new unlock rule.
     *
     * @param request the create request
     * @return the created unlock rule
     */
    UnlockRuleResponse createUnlockRule(CreateUnlockRuleRequest request);

    /**
     * Update an existing unlock rule.
     *
     * @param id      the unlock rule ID
     * @param request the update request
     * @return the updated unlock rule
     */
    UnlockRuleResponse updateUnlockRule(String id, UpdateUnlockRuleRequest request);

    /**
     * Delete an unlock rule (soft delete).
     *
     * @param id the unlock rule ID
     */
    void deleteUnlockRule(String id);

    /**
     * Get unlock rule by ID.
     *
     * @param id the unlock rule ID
     * @return the unlock rule
     */
    UnlockRuleResponse getUnlockRuleById(String id);

    /**
     * Get all unlock rules for a specific StudySet.
     *
     * @param studySetId the StudySet ID that needs to be unlocked
     * @return list of unlock rules
     */
    List<UnlockRuleResponse> getUnlockRulesByStudySet(String studySetId);

    /**
     * Find which StudySets will unlock after completing the given StudySet.
     *
     * @param studySetId the completed StudySet ID
     * @return list of unlock rules where this StudySet is required
     */
    List<UnlockRuleResponse> getUnlockRulesRequiringStudySet(String studySetId);
}
