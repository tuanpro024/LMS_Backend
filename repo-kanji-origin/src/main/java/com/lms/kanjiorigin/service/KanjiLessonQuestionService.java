package com.lms.kanjiorigin.service;

import com.lms.kanjiorigin.dto.request.CreateQuestionRequest;
import com.lms.kanjiorigin.dto.request.UpdateQuestionRequest;
import com.lms.kanjiorigin.dto.response.QuestionResponse;

import java.util.List;

public interface KanjiLessonQuestionService {
    QuestionResponse createQuestion(CreateQuestionRequest request);
    QuestionResponse updateQuestion(String id, UpdateQuestionRequest request);
    void deleteQuestion(String id);
    List<QuestionResponse> getQuestionsByLesson(String lessonId);
}
