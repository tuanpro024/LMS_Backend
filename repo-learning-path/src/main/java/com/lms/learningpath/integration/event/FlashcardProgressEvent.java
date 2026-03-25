package com.lms.learningpath.integration.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FlashcardProgressEvent(
        String eventId,
        String userId,
        String studySetId,
        Integer learnedCards,
        Integer totalCards,
        Double progressPercentage,
        Boolean completed,
        Instant occurredAt) {
}
