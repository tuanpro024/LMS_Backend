package com.lms.pronunciation.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PronunciationStudySetProgressKafkaPayload {
    private String eventId;
    private String userId;
    private String studySetId;
    private int learnedItems;
    private int totalItems;
    private double progressPercentage;
    private boolean completed;
    private Instant completedAt;
    private Instant occurredAt;
}
