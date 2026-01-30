package com.lms.learningpath.service;

import com.lms.learningpath.dto.response.ModuleProgressDto;

/**
 * Service interface for sending realtime notifications to users.
 */
public interface IRealtimeNotificationService {

    /**
     * Notify user of progress update.
     *
     * @param userId   the user ID
     * @param progress the module progress DTO
     */
    void notifyProgressUpdate(String userId, ModuleProgressDto progress);

    /**
     * Notify user that a module is completed.
     *
     * @param userId   the user ID
     * @param progress the completed module progress DTO
     */
    void notifyModuleCompleted(String userId, ModuleProgressDto progress);

    /**
     * Notify user that a StudySet is completed.
     *
     * @param userId     the user ID
     * @param studySetId the completed StudySet ID
     */
    void notifyStudySetCompleted(String userId, String studySetId);

    /**
     * Notify user that a StudySet is unlocked.
     *
     * @param userId     the user ID
     * @param studySetId the unlocked StudySet ID
     */
    void notifyStudySetUnlocked(String userId, String studySetId);
}
