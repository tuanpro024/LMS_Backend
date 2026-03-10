package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.kanjiorigin.dto.request.CreateQuestionRequest;
import com.lms.kanjiorigin.dto.request.KanjiQuestionSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateQuestionRequest;
import com.lms.kanjiorigin.dto.response.QuestionResponse;
import com.lms.kanjiorigin.service.KanjiQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kanji-questions")
@RequiredArgsConstructor
public class KanjiQuestionController {

    private final KanjiQuestionService questionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        return ApiResponse.ok(questionService.createQuestion(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody UpdateQuestionRequest request) {
        return ApiResponse.ok(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<Void> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionResponse> getQuestion(@PathVariable String id) {
        return ApiResponse.ok(questionService.getQuestionById(id));
    }

    @GetMapping
    public ApiResponse<List<QuestionResponse>> search(KanjiQuestionSearchRequest request) {
        return ApiResponse.ok(questionService.search(request));
    }

    @GetMapping("/paged")
    public ApiResponse<PageResponse<QuestionResponse>> searchPaged(KanjiQuestionSearchRequest request) {
        return ApiResponse.ok(questionService.searchPaged(request));
    }
}
