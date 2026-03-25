package com.lms.writing.event;

import lombok.Builder;

import java.time.Instant;

@Builder
public record WritingProgressUpdatedEvent(
        String eventId,
        String userId,
        String studySetId,
        Integer learnedWords,
        Integer totalWords,
        Double progressPercentage,
        Boolean completed,
        Instant occurredAt) {
}