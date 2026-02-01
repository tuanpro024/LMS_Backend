package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.kanjiorigin.service.KanjiLessonProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kanji-lesson-progress")
@RequiredArgsConstructor
public class KanjiLessonProgressController {

    private final KanjiLessonProgressService progressService;

    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<ApiResponse<Void>> markLessonAsLearned(
            @PathVariable String lessonId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        progressService.markLessonAsLearned(lessonId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{lessonId}/complete")
    public ResponseEntity<ApiResponse<Void>> unmarkLessonAsLearned(
            @PathVariable String lessonId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        progressService.unmarkLessonAsLearned(lessonId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getLearnedLessonIds(
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(progressService.getLearnedLessonIds(principal.userId())));
    }
}
