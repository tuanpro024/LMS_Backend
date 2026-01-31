package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.kanjiorigin.dto.request.CreateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.response.KanjiLessonBasicResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonResponse;
import com.lms.kanjiorigin.service.KanjiLessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kanji-lessons")
@RequiredArgsConstructor
public class KanjiLessonController {

    private final KanjiLessonService kanjiLessonService;

    @PostMapping
    public ApiResponse<KanjiLessonResponse> createLesson(@Valid @RequestBody CreateKanjiLessonRequest request) {
        return ApiResponse.ok(kanjiLessonService.createLesson(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<KanjiLessonResponse> updateLesson(
            @PathVariable String id,
            @Valid @RequestBody UpdateKanjiLessonRequest request) {
        return ApiResponse.ok(kanjiLessonService.updateLesson(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLesson(@PathVariable String id) {
        kanjiLessonService.deleteLesson(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<KanjiLessonResponse> getLesson(@PathVariable String id) {
        return ApiResponse.ok(kanjiLessonService.getLesson(id));
    }

    @GetMapping
    public ApiResponse<List<KanjiLessonBasicResponse>> getAllLessons() {
        return ApiResponse.ok(kanjiLessonService.getAllLessons());
    }
}
