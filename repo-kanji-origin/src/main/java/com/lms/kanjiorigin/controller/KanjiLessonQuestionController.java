package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.kanjiorigin.dto.request.AssignQuestionToMultipleLessonsRequest;
import com.lms.kanjiorigin.dto.response.LessonQuestionAssignmentResponse;
import com.lms.kanjiorigin.service.KanjiLessonQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kanji-lesson-questions")
@RequiredArgsConstructor
public class KanjiLessonQuestionController {

    private final KanjiLessonQuestionService lessonQuestionService;

    @PostMapping
    public ApiResponse<List<LessonQuestionAssignmentResponse>> assignQuestionToLessons(
            @Valid @RequestBody AssignQuestionToMultipleLessonsRequest request) {
        return ApiResponse.ok(lessonQuestionService.assignQuestionToMultipleLessons(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeAssignment(@PathVariable String id) {
        lessonQuestionService.removeQuestionFromLesson(id);
        return ApiResponse.ok(null);
    }
}

