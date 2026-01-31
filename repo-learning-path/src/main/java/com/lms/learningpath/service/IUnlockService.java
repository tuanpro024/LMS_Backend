package com.lms.learningpath.service;

import com.lms.content.common.entity.StudySet;

/**
 * Service interface for managing StudySet unlock logic.
 */
public interface IUnlockService {

    /**
     * Check if a StudySet is unlocked for a user.
     *
     * @param userId     the user ID
     * @param studySetId the StudySet ID
     * @return true if unlocked, false otherwise
     */
    boolean isStudySetUnlocked(String userId, String studySetId);

    /**
     * Get the lock reason for a StudySet.
     *
     * @param userId     the user ID
     * @param studySetId the StudySet ID
     * @return lock reason message, or null if unlocked
     */
    String getLockReason(String userId, String studySetId);

    /**
     * Check and unlock the next StudySet after a user completes one.
     *
     * @param userId       the user ID
     * @param completedSet the completed StudySet
     */
    void checkAndUnlockNextStudySet(String userId, StudySet completedSet);
}
