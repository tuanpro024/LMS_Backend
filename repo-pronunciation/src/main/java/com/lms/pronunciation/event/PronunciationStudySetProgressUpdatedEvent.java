package com.lms.pronunciation.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PronunciationStudySetProgressUpdatedEvent {
    private final String userId;
    private final String studySetId;
    private final int learnedItems;
    private final int totalItems;
    private final double progressPercentage;
    private final boolean completed;
    private final Instant completedAt;
    private final Instant occurredAt;
}
