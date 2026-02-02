package com.lms.kanjiorigin.service;

import com.lms.kanjiorigin.dto.request.AssignQuestionToLessonRequest;
import com.lms.kanjiorigin.dto.request.AssignQuestionToMultipleLessonsRequest;
import com.lms.kanjiorigin.dto.response.LessonQuestionAssignmentResponse;

import java.util.List;

public interface KanjiLessonQuestionService {
    LessonQuestionAssignmentResponse assignQuestionToLesson(AssignQuestionToLessonRequest request);
    List<LessonQuestionAssignmentResponse> assignQuestionToMultipleLessons(AssignQuestionToMultipleLessonsRequest request);
    void removeQuestionFromLesson(String assignmentId);
    List<LessonQuestionAssignmentResponse> getQuestionsByLesson(String lessonId);
}
