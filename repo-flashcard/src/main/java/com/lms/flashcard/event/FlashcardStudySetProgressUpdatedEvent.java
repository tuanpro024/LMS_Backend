package com.lms.flashcard.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class FlashcardStudySetProgressUpdatedEvent {
    private final String userId;
    private final String studySetId;
    private final int learnedCards;
    private final int totalCards;
    private final double progressPercentage;
    private final boolean completed;
    private final Instant completedAt;
    private final Instant occurredAt;
}
