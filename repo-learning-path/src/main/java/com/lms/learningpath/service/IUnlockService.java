package com.lms.learningpath.service;

/**
 * Service interface for managing Step unlock logic in the new step-based
 * hierarchy.
 */
public interface IUnlockService {

    /**
     * Check if a Step is unlocked for a user based on previous step completion.
     *
     * @param userId the user ID
     * @param stepId the Step ID
     * @return true if unlocked, false otherwise
     */
    boolean isStepUnlocked(String userId, String stepId);

    /**
     * Get the lock reason for a Step.
     *
     * @param userId the user ID
     * @param stepId the Step ID
     * @return lock reason message, or null if unlocked
     */
    String getLockReason(String userId, String stepId);
}
