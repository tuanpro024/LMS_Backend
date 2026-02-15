package com.lms.learningpath.dto.response.quiz;

import com.lms.learningpath.dto.request.quiz.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Summary response for quiz listing (without questions).
 * Copied from repo-quiz for import functionality.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {

    private String id;
    private String studySetId;
    private String title;
    private String description;
    private DifficultyLevel difficulty;
    private Integer timeLimitSeconds;
    private Integer passingScore;
    private Integer totalQuestions;
    private Instant createdAt;
    private Instant updatedAt;
}
