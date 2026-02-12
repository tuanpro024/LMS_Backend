package com.lms.quiz.dto.request;

import com.lms.quiz.entity.enums.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {

    @NotBlank(message = "Study set ID is required")
    private String studySetId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String instruction;
    private DifficultyLevel difficulty;
    private Integer timeLimitSeconds;
    private Integer passingScore;
    private Boolean shuffleQuestions;

    private List<CreateQuestionRequest> questions;
}
