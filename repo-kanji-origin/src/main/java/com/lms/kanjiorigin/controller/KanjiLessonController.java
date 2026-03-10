package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.kanjiorigin.dto.request.CreateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.request.KanjiLessonSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.response.ImportResultResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonBasicResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonResponse;
import com.lms.kanjiorigin.service.KanjiLessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/kanji-lessons")
@RequiredArgsConstructor
public class KanjiLessonController {

    private final KanjiLessonService kanjiLessonService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<KanjiLessonResponse> createLesson(@Valid @RequestBody CreateKanjiLessonRequest request) {
        return ApiResponse.ok(kanjiLessonService.createLesson(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<KanjiLessonResponse> updateLesson(
            @PathVariable String id,
            @Valid @RequestBody UpdateKanjiLessonRequest request) {
        return ApiResponse.ok(kanjiLessonService.updateLesson(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<Void> deleteLesson(@PathVariable String id) {
        kanjiLessonService.deleteLesson(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<KanjiLessonResponse> getLesson(@PathVariable String id) {
        return ApiResponse.ok(kanjiLessonService.getLesson(id));
    }

    @GetMapping
    public ApiResponse<List<KanjiLessonBasicResponse>> search(KanjiLessonSearchRequest request) {
        return ApiResponse.ok(kanjiLessonService.search(request));
    }

    @GetMapping("/paged")
    public ApiResponse<PageResponse<KanjiLessonBasicResponse>> searchPaged(KanjiLessonSearchRequest request) {
        return ApiResponse.ok(kanjiLessonService.searchPaged(request));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<ImportResultResponse> importFromExcel(
            @RequestParam("studySetId") String studySetId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(kanjiLessonService.importFromExcel(studySetId, file));
    }
}
