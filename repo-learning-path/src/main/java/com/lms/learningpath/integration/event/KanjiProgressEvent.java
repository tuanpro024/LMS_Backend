package com.lms.learningpath.integration.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KanjiProgressEvent(
        String eventId,
        String userId,
        String studySetId,
        Integer learnedLessons,
        Integer totalLessons,
        Double progressPercentage,
        Boolean completed,
        Instant occurredAt) {
}
