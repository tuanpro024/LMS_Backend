package com.lms.quiz.service;

import com.lms.quiz.dto.request.CreateQuizRequest;
import com.lms.quiz.dto.response.QuizDetailResponse;
import com.lms.quiz.dto.response.QuizResponse;

import java.util.List;

public interface IQuizService {

    QuizDetailResponse createQuiz(CreateQuizRequest request, String userId);

    QuizDetailResponse getQuizById(String quizId);

    /**
     * Lấy quiz để user làm bài - KHÔNG chứa đáp án đúng
     */
    QuizDetailResponse getQuizForAttempt(String quizId);

    List<QuizResponse> getQuizzesByStudySetId(String studySetId);

    QuizDetailResponse updateQuiz(String quizId, CreateQuizRequest request, String userId);

    void deleteQuiz(String quizId, String userId);
}
