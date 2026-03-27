package com.lms.kanjiorigin.event;

import lombok.Builder;

import java.time.Instant;

@Builder
public record KanjiStudySetProgressUpdatedEvent(
        String eventId,
        String userId,
        String studySetId,
        Integer learnedLessons,
        Integer totalLessons,
        Double progressPercentage,
        Boolean completed,
        Instant occurredAt) {
}
