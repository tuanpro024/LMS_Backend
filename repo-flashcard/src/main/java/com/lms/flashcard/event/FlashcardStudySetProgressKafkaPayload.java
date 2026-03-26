package com.lms.flashcard.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardStudySetProgressKafkaPayload {
    private String eventId;
    private String userId;
    private String studySetId;
    private int learnedCards;
    private int totalCards;
    private double progressPercentage;
    private boolean completed;
    private Instant completedAt;
    private Instant occurredAt;
}
