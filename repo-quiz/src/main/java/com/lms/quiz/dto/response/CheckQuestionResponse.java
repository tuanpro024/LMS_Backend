package com.lms.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kết quả chấm 1 câu hỏi từ backend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckQuestionResponse {

    private String quizId;
    private String questionId;
    private String questionType;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private Integer maxPoints;
    private Integer timeTakenSeconds;
    private String explanation;

    private Object correctAnswer;
    private Object userAnswer;
}