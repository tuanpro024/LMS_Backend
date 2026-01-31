package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.kanjiorigin.dto.request.CreateQuestionRequest;
import com.lms.kanjiorigin.dto.request.UpdateQuestionRequest;
import com.lms.kanjiorigin.dto.response.QuestionResponse;
import com.lms.kanjiorigin.service.KanjiLessonQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kanji-questions")
@RequiredArgsConstructor
public class KanjiLessonQuestionController {

    private final KanjiLessonQuestionService questionService;

    @PostMapping
    public ApiResponse<QuestionResponse> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        return ApiResponse.ok(questionService.createQuestion(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionResponse> updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody UpdateQuestionRequest request) {
        return ApiResponse.ok(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<QuestionResponse>> getQuestionsByLesson(@RequestParam String lessonId) {
        return ApiResponse.ok(questionService.getQuestionsByLesson(lessonId));
    }
}
