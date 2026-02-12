package com.lms.quiz.service;

import com.lms.quiz.dto.request.SubmitQuizRequest;
import com.lms.quiz.dto.response.QuizResultResponse;

public interface IQuizAttemptService {

    /**
     * Chấm điểm bài quiz khi user submit.
     * Logic chấm theo từng questionType:
     * - MULTIPLE_CHOICE: so khớp selectedOptionId với option.isCorrect
     * - FILL_IN_BLANK: so khớp từng blank (case-insensitive, trim)
     * - MATCHING_PAIRS: kiểm tra từng cặp nối
     * - SENTENCE_BUILDER: so khớp thứ tự chunks
     */
    QuizResultResponse submitQuiz(SubmitQuizRequest request, String userId);
}
