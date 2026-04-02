package com.lms.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Update 1 question's answer/result based on previous result snapshot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionResultRequest {

    private String quizId;
    private SubmitQuizRequest.SubmitAnswerRequest answer;
    private List<PreviousQuestionResult> previousQuestionResults;
    private Integer timeTakenSeconds;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviousQuestionResult {
        private String questionId;
        private String questionType;
        private Object userAnswer;
    }
}
