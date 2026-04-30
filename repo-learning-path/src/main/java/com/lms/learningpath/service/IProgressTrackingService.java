package com.lms.learningpath.service;

import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.UpdateProgressRequest;
import com.lms.learningpath.dto.response.ModuleProgressDto;
import com.lms.learningpath.dto.response.StepProgressResponse;
import com.lms.learningpath.dto.response.LearningPathProgressResponse;

import java.util.List;

/**
 * Service interface for tracking user progress on modules, steps, and learning
 * paths.
 * Updated for the new step-based hierarchy.
 */
public interface IProgressTrackingService {

    /**
     * Start a module for a user (step module).
     *
     * @param userId   the user ID
     * @param moduleId the step module ID
     * @return module progress DTO
     */
    ModuleProgressDto startModule(String userId, String moduleId);

    /**
     * Update module progress.
     *
     * @param userId   the user ID
     * @param moduleId the step module ID
     * @param request  update request
     * @return updated module progress DTO
     */
    ModuleProgressDto updateProgress(String userId, String moduleId, UpdateProgressRequest request);

    /**
     * Complete a module.
     *
     * @param userId   the user ID
     * @param moduleId the step module ID
     * @param request  completion request
     * @return completed module progress DTO
     */
    ModuleProgressDto completeModule(String userId, String moduleId, CompleteModuleRequest request);

    /**
     * Get progress for a specific module.
     *
     * @param userId   the user ID
     * @param moduleId the step module ID
     * @return module progress DTO
     */
    ModuleProgressDto getModuleProgress(String userId, String moduleId);

    /**
     * Get step progress for a user.
     *
     * @param userId the user ID
     * @param stepId the step ID
     * @return step progress response
     */
    StepProgressResponse getStepProgress(String userId, String stepId);

    /**
     * Get all module progress records for a user in a study set.
     *
     * @param userId the user ID
     * @param studySetId the study set ID
     * @return list of module progress records
     */
    List<ModuleProgressDto> getStudySetModulesProgress(String userId, String studySetId);

    /**
     * Get learning path progress for a user.
     *
     * @param userId         the user ID
     * @param learningPathId the learning path ID
     * @return learning path progress response
     */
    LearningPathProgressResponse getLearningPathProgress(String userId, String learningPathId);

    /**
     * Get all learning path progress for a user in a study set.
     *
     * @param userId     the user ID
     * @param studySetId the study set ID
     * @return list of learning path progress
     */
    List<LearningPathProgressResponse> getAllLearningPathProgress(String userId, String studySetId);
}
