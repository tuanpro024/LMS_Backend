package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.videocourse.dto.request.CompleteWatchRequest;
import com.lms.videocourse.dto.request.UpdateWatchProgressRequest;
import com.lms.videocourse.dto.response.VideoCourseProgressResponse;
import com.lms.videocourse.dto.response.VideoStepProgressResponse;
import com.lms.videocourse.dto.response.VideoWatchProgressResponse;
import com.lms.videocourse.service.IVideoProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user video watch progress.
 *
 * All endpoints require authentication.
 *
 * Key flow:
 * 1. GET /progress/modules/{moduleId}/start — start watching (creates progress
 * record)
 * 2. PUT /progress/modules/{moduleId}/watch — update watched seconds
 * (auto-completes at 80%)
 * 3. POST /progress/modules/{moduleId}/complete — force-complete regardless of
 * watch %
 * 4. GET /progress/modules/{moduleId} — get current watch progress
 * 5. GET /progress/steps/{stepId} — step-level aggregated progress
 * 6. GET /progress/courses/{courseId} — course-level aggregated progress
 */
@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class VideoProgressController {

    private final IVideoProgressService videoProgressService;

    // ===== Module (Watch) Progress =====

    @GetMapping("/modules/{moduleId}/start")
    public ResponseEntity<ApiResponse<VideoWatchProgressResponse>> startWatching(
            @PathVariable String moduleId,
            Authentication authentication) {

        String userId = getUserId(authentication);
        VideoWatchProgressResponse response = videoProgressService.startWatching(userId, moduleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/modules/{moduleId}/watch")
    public ResponseEntity<ApiResponse<VideoWatchProgressResponse>> updateWatchProgress(
            @PathVariable String moduleId,
            @RequestBody @Valid UpdateWatchProgressRequest request,
            Authentication authentication) {

        String userId = getUserId(authentication);
        VideoWatchProgressResponse response = videoProgressService.updateWatchProgress(userId, moduleId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/modules/{moduleId}/complete")
    public ResponseEntity<ApiResponse<VideoWatchProgressResponse>> completeWatch(
            @PathVariable String moduleId,
            @RequestBody CompleteWatchRequest request,
            Authentication authentication) {

        String userId = getUserId(authentication);
        VideoWatchProgressResponse response = videoProgressService.completeWatch(userId, moduleId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<ApiResponse<VideoWatchProgressResponse>> getWatchProgress(
            @PathVariable String moduleId,
            Authentication authentication) {

        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(videoProgressService.getWatchProgress(userId, moduleId)));
    }

    // ===== Step Progress =====

    @GetMapping("/steps/{stepId}")
    public ResponseEntity<ApiResponse<VideoStepProgressResponse>> getStepProgress(
            @PathVariable String stepId,
            Authentication authentication) {

        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(videoProgressService.getStepProgress(userId, stepId)));
    }

    // ===== Course Progress =====

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<VideoCourseProgressResponse>> getCourseProgress(
            @PathVariable String courseId,
            Authentication authentication) {

        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(videoProgressService.getCourseProgress(userId, courseId)));
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<VideoCourseProgressResponse>>> getAllCourseProgress(
            @RequestParam String studySetId,
            Authentication authentication) {

        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(videoProgressService.getAllCourseProgress(userId, studySetId)));
    }

    // ===== Helper =====

    private String getUserId(Authentication authentication) {
        return ((AuthPrincipal) authentication.getPrincipal()).userId();
    }
}
