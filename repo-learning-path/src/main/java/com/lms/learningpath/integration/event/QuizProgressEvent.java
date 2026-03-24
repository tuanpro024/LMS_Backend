package com.lms.learningpath.integration.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuizProgressEvent(
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
