package com.lms.videocourse.service;

public interface IVideoUnlockService {

    /**
     * Check if a VideoStep is unlocked for a user.
     * Step 1 is always unlocked.
     * Step N requires Step N-1 to be COMPLETED.
     */
    boolean isStepUnlocked(String userId, String stepId);

    /**
     * Get the reason why a step is locked (null if unlocked).
     */
    String getLockReason(String userId, String stepId);
}
