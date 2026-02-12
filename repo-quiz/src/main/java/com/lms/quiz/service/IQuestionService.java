package com.lms.quiz.service;

import com.lms.quiz.dto.request.CreateQuestionRequest;
import com.lms.quiz.dto.response.QuestionResponse;

import java.util.List;

public interface IQuestionService {

    QuestionResponse addQuestion(String quizId, CreateQuestionRequest request, String userId);

    List<QuestionResponse> getQuestionsByQuizId(String quizId);

    QuestionResponse updateQuestion(String questionId, CreateQuestionRequest request, String userId);

    void deleteQuestion(String questionId, String userId);
}
