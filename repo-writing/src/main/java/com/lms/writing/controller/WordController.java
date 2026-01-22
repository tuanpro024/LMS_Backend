package com.lms.writing.controller;

import com.lms.common.security.AuthPrincipal;
import org.springframework.security.core.Authentication;
import com.lms.common.dto.ApiResponse;
import com.lms.writing.dto.request.UpdateWordRequest;
import com.lms.writing.dto.response.WordResponse;
import com.lms.writing.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
@Slf4j
public class WordController {

    private final WordService wordService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WordResponse>> getWord(@PathVariable String id) {
        WordResponse response = wordService.getWordById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/study-set")
    public ResponseEntity<ApiResponse<List<WordResponse>>> getWordsByStudySet(
            @RequestParam String studySetId) {
        List<WordResponse> response = wordService.getWordsByStudySetId(studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WordResponse>> updateWord(
            @PathVariable String id,
            @Valid @RequestBody UpdateWordRequest request,
            @RequestHeader("X-User-Id") String userId) {
        WordResponse response = wordService.updateWord(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateWordStatus(
            @PathVariable String id,
            @RequestBody @Valid com.lms.writing.dto.request.UpdateWordStatusRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        wordService.updateWordStatus(principal.userId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWord(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        wordService.deleteWord(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
