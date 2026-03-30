package com.lms.quiz.event;

import lombok.Builder;

import java.time.Instant;

@Builder
public record QuizStudySetProgressUpdatedEvent(
        String eventId,
        String userId,
        String studySetId,
        Integer completedQuizzes,
        Integer totalQuizzes,
        Double progressPercentage,
        Boolean completed,
        Instant completedAt,
        Instant occurredAt) {
}
