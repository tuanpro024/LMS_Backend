package com.lms.learningpath.service;

import com.lms.learningpath.dto.response.SectionModuleResponse;

import java.util.List;

/**
 * Service for tracking and updating user progress through learning paths.
 */
public interface ProgressTrackingService {

    /**
     * Mark a module as completed for a user
     */
    void completeModule(String userId, String moduleId);

    /**
     * Update progress for a section based on completed modules
     */
    void updateSectionProgress(String userId, String folderId);

    /**
     * Check if a section is completed (all required modules done)
     */
    boolean isSectionCompleted(String userId, String folderId);

    /**
     * Get completion percentage for a section
     */
    double getSectionCompletionPercentage(String userId, String folderId);

    /**
     * Get completion percentage for entire learning path
     */
    double getLearningPathCompletionPercentage(String userId, String packageId);

    /**
     * Initialize progress tracking for a user starting a learning path
     */
    void initializeLearningPathProgress(String userId, String packageId);

    /**
     * Get enriched module data with progress information
     */
    List<SectionModuleResponse> getModulesWithProgress(String userId, String folderId);
}
