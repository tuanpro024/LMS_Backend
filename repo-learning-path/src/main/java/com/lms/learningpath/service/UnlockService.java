package com.lms.learningpath.service;

/**
 * Service for handling section unlock logic.
 * Determines which sections are accessible to users based on completion status.
 */
public interface UnlockService {

    /**
     * Check if a section (folder) is unlocked for a user
     */
    boolean isSectionUnlocked(String userId, String folderId);

    /**
     * Get the prerequisite section ID for a folder
     */
    String getPrerequisiteSection(String folderId);

    /**
     * Unlock next section when current section is completed
     */
    void unlockNextSection(String userId, String packageId, String completedFolderId);
}
