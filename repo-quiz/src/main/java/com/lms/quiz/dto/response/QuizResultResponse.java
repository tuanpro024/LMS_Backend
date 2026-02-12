package com.lms.quiz.dto.response;

import lombok.*;

import java.util.List;

/**
 * Kết quả sau khi user submit bài quiz.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {

    private String quizId;
    private String quizTitle;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer totalPoints;
    private Integer earnedPoints;
    private Double scorePercentage;
    private Boolean passed;
    private Integer timeTakenSeconds;

    private List<QuestionResult> questionResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResult {
        private String questionId;
        private String questionType;
        private Boolean isCorrect;
        private Integer pointsEarned;
        private String explanation;

        private Object correctAnswer;
        private Object userAnswer;
    }
}
