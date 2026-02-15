package com.lms.learningpath.dto.request.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a quiz.
 * Copied from repo-quiz for import functionality.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {

    private String studySetId;
    private String title;
    private String description;
    private String instruction;
    private DifficultyLevel difficulty;
    private Integer timeLimitSeconds;
    private Integer passingScore;
    private Boolean shuffleQuestions;

    private List<CreateQuestionRequest> questions;
}
