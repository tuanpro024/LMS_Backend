package com.lms.quiz.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizProgressResponse {

    private String userId;
    private String quizId;
    private String studySetId;

    private Integer attemptsCount;

    private Double latestScorePercentage;
    private Double bestScorePercentage;

    private Boolean completed;

    private Instant firstAttemptAt;
    private Instant lastAttemptAt;
    private Instant completedAt;
}
