package com.lms.learningpath.integration.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WritingProgressEvent(
        String eventId,
        String userId,
        String studySetId,
        Integer learnedWords,
        Integer totalWords,
        Double progressPercentage,
        Boolean completed,
        Instant occurredAt) {
}