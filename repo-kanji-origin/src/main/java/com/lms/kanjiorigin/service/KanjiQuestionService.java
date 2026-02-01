package com.lms.kanjiorigin.service;

import com.lms.common.dto.PageResponse;
import com.lms.kanjiorigin.dto.request.CreateQuestionRequest;
import com.lms.kanjiorigin.dto.request.KanjiQuestionSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateQuestionRequest;
import com.lms.kanjiorigin.dto.response.QuestionResponse;

import java.util.List;

public interface KanjiQuestionService {
    QuestionResponse createQuestion(CreateQuestionRequest request);
    QuestionResponse updateQuestion(String id, UpdateQuestionRequest request);
    void deleteQuestion(String id);
    QuestionResponse getQuestionById(String id);
    List<QuestionResponse> getAllQuestions();
    List<QuestionResponse> getQuestionsByLessonId(String lessonId);
    
    List<QuestionResponse> search(KanjiQuestionSearchRequest request);
    
    PageResponse<QuestionResponse> searchPaged(KanjiQuestionSearchRequest request);
}
