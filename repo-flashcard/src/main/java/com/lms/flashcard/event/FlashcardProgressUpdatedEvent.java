package com.lms.flashcard.event;

import lombok.Builder;

import java.time.Instant;

@Builder
public record FlashcardProgressUpdatedEvent(
        String eventId,
        String userId,
        String studySetId,
        Integer learnedCards,
        Integer totalCards,
        Double progressPercentage,
        Boolean completed,
        Instant occurredAt) {
}
