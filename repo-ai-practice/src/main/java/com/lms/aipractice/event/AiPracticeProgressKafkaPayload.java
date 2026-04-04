package com.lms.aipractice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Serializable Kafka payload for AI practice progress events.
 * Published to topic: ai-practice.progress.events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPracticeProgressKafkaPayload {
    private String eventId;
    private String userId;
    private String studySetId;
    private String attemptId;
    private int gradedAnswers;
    private int totalAnswers;
    private double totalScore;
    private double maxScore;
    private double progressPercent;
    private boolean completed;
    private Instant occurredAt;
}
