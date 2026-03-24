package com.lms.quiz.event;

import lombok.Builder;

import java.time.Instant;

@Builder
public record QuizAttemptSubmittedEvent(
        String eventId,
        String attemptId,
        String userId,
        String studySetId,
        String quizId,
        String quizTitle,
        Integer attemptsCount,
        Integer earnedPoints,
        Integer totalPoints,
        Double scorePercentage,
        Boolean passed,
        Integer timeTakenSeconds,
        Instant occurredAt) {
}
