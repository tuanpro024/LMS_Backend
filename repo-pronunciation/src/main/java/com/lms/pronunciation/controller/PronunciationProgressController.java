package com.lms.pronunciation.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.pronunciation.dto.response.PronunciationItemProgressResponse;
import com.lms.pronunciation.dto.response.PronunciationStudySetProgressResponse;
import com.lms.pronunciation.service.PronunciationProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pronunciation-progress")
@RequiredArgsConstructor
public class PronunciationProgressController {

    private final PronunciationProgressService progressService;

    @PostMapping("/items/{itemId}/listen")
    public ApiResponse<PronunciationItemProgressResponse> markItemListened(
            @PathVariable String itemId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(progressService.markItemListened(principal.userId(), itemId));
    }

    @GetMapping("/study-sets/{studySetId}")
    public ApiResponse<PronunciationStudySetProgressResponse> getStudySetProgress(
            @PathVariable String studySetId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(progressService.getStudySetProgress(principal.userId(), studySetId));
    }

    @GetMapping("/study-sets/{studySetId}/items")
    public ApiResponse<List<PronunciationItemProgressResponse>> getItemProgressByStudySet(
            @PathVariable String studySetId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(progressService.getItemProgressByStudySet(principal.userId(), studySetId));
    }
}
