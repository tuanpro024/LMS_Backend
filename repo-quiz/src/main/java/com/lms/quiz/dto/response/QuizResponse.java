package com.lms.quiz.dto.response;

import com.lms.quiz.entity.enums.DifficultyLevel;
import lombok.*;

import java.time.Instant;

/**
 * Summary response cho quiz listing (không bao gồm câu hỏi).
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
