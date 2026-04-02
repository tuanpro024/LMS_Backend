package com.lms.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User kiểm tra từng câu hỏi trong lúc làm quiz.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckQuestionRequest {

    private String quizId;
    private SubmitQuizRequest.SubmitAnswerRequest answer;
    private Integer timeTakenSeconds;
}