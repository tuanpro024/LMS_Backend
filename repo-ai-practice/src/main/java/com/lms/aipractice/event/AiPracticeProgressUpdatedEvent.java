package com.lms.aipractice.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Spring ApplicationEvent — published internally when an attempt is graded.
 * The Kafka publisher listens to this event and forwards it to the message broker.
 */
@Getter
@Builder
public class AiPracticeProgressUpdatedEvent {
    private final String userId;
    private final String studySetId;
    private final String attemptId;
    private final int gradedAnswers;
    private final int totalAnswers;
    private final double totalScore;
    private final double maxScore;
    private final double progressPercent;
    private final boolean completed;
    private final Instant occurredAt;
}
