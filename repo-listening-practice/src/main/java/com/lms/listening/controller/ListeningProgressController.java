package com.lms.listening.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.listening.dto.request.UpdateListeningProgressRequest;
import com.lms.listening.dto.response.ListeningStudySetProgressResponse;
import com.lms.listening.dto.response.ListeningVideoProgressResponse;
import com.lms.listening.service.ListeningProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listening-practice/progress")
@RequiredArgsConstructor
public class ListeningProgressController {

    private final ListeningProgressService progressService;

    private String getUserId(Authentication authentication) {
        return (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
    }

    @PostMapping("/study-sets/{studySetId}/videos/{videoCode}/start")
    public ResponseEntity<ApiResponse<ListeningVideoProgressResponse>> startWatching(
            @PathVariable String studySetId,
            @PathVariable String videoCode,
            Authentication authentication) {
        String userId = getUserId(authentication);
        ListeningVideoProgressResponse response = progressService.startWatching(userId, studySetId, videoCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/study-sets/{studySetId}/videos/{videoCode}/update")
    public ResponseEntity<ApiResponse<ListeningVideoProgressResponse>> updateProgress(
            @PathVariable String studySetId,
            @PathVariable String videoCode,
            @Valid @RequestBody UpdateListeningProgressRequest request,
            Authentication authentication) {
        String userId = getUserId(authentication);
        ListeningVideoProgressResponse response = progressService.updateWatchProgress(userId, studySetId, videoCode, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/study-sets/{studySetId}/videos/{videoCode}/complete")
    public ResponseEntity<ApiResponse<ListeningVideoProgressResponse>> forceComplete(
            @PathVariable String studySetId,
            @PathVariable String videoCode,
            Authentication authentication) {
        String userId = getUserId(authentication);
        ListeningVideoProgressResponse response = progressService.forceComplete(userId, studySetId, videoCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/study-sets/{studySetId}/videos/{videoCode}")
    public ResponseEntity<ApiResponse<ListeningVideoProgressResponse>> getVideoProgress(
            @PathVariable String studySetId,
            @PathVariable String videoCode,
            Authentication authentication) {
        String userId = getUserId(authentication);
        ListeningVideoProgressResponse response = progressService.getVideoProgress(userId, studySetId, videoCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<ListeningStudySetProgressResponse>> getStudySetProgress(
            @PathVariable String studySetId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        ListeningStudySetProgressResponse response = progressService.getStudySetProgress(userId, studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
