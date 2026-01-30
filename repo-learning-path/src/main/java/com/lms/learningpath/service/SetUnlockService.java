package com.lms.learningpath.service;

import com.lms.learningpath.dto.response.UnlockedSetInfo;

import java.util.List;

public interface SetUnlockService {

    /**
     * Kiểm tra study set có unlock được không
     */
    boolean canUnlockSet(String userId, String studySetId);

    /**
     * Unlock study set tiếp theo sau khi hoàn thành set hiện tại
     */
    List<UnlockedSetInfo> unlockNextSets(String userId, String completedSetId);

    /**
     * Unlock study set cho user (admin)
     */
    void unlockSetForUser(String userId, String studySetId);
}