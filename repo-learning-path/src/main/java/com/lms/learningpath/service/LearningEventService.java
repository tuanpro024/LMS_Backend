package com.lms.learningpath.service;

import com.lms.learningpath.entity.enums.EventType;

public interface LearningEventService {

    /**
     * Log event học tập
     */
    void logEvent(
            String userId,
            EventType eventType,
            String studySetId,
            String moduleId,
            Integer score,
            Integer durationSeconds,
            String metadata
    );

    /**
     * Log event đơn giản (không cần score/duration)
     */
    void logSimpleEvent(
            String userId,
            EventType eventType,
            String studySetId,
            String moduleId
    );
}