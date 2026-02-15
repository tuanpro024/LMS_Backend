package com.lms.learningpath.dto.response.quiz;

import com.lms.learningpath.dto.request.quiz.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Detailed quiz response with questions.
 * Copied from repo-quiz for import functionality.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDetailResponse {

    private String id;
    private String studySetId;
    private String title;
    private String description;
    private String instruction;
    private DifficultyLevel difficulty;
    private Integer timeLimitSeconds;
    private Integer passingScore;
    private Boolean shuffleQuestions;
    private Integer totalQuestions;
    private Integer totalPoints;
    private List<QuestionResponse> questions;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Nested question response (minimal - only what's needed for import).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResponse {
        private String id;
        private String questionText;
        private Integer points;
    }
}
