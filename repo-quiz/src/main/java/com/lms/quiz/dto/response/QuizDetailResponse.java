package com.lms.quiz.dto.response;

import com.lms.quiz.entity.enums.DifficultyLevel;
import lombok.*;

import java.time.Instant;
import java.util.List;

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
}
