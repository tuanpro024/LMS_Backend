package com.lms.learningpath.service;

import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.UpdateProgressRequest;
import com.lms.learningpath.dto.response.ModuleProgressDto;
import com.lms.learningpath.dto.response.StudySetModuleResponse;
import com.lms.learningpath.dto.response.StudySetProgressDto;

import java.util.List;

/**
 * Service interface for tracking user progress on modules and StudySets.
 */
public interface IProgressTrackingService {

    /**
     * Start a module for a user.
     *
     * @param userId   the user ID
     * @param moduleId the module ID
     * @return module progress DTO
     */
    ModuleProgressDto startModule(String userId, String moduleId);

    /**
     * Update module progress.
     *
     * @param userId   the user ID
     * @param moduleId the module ID
     * @param request  update request
     * @return updated module progress DTO
     */
    ModuleProgressDto updateProgress(String userId, String moduleId, UpdateProgressRequest request);

    /**
     * Complete a module.
     *
     * @param userId   the user ID
     * @param moduleId the module ID
     * @param request  completion request
     * @return completed module progress DTO
     */
    ModuleProgressDto completeModule(String userId, String moduleId, CompleteModuleRequest request);

    /**
     * Get modules with user progress for a StudySet.
     *
     * @param userId     the user ID
     * @param studySetId the StudySet ID
     * @return list of modules with progress
     */
    List<StudySetModuleResponse> getModulesWithProgress(String userId, String studySetId);

    /**
     * Get StudySet progress with lock status.
     *
     * @param userId     the user ID
     * @param studySetId the StudySet ID
     * @return StudySet progress DTO
     */
    StudySetProgressDto getStudySetProgress(String userId, String studySetId);
}
