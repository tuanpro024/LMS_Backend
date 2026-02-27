package com.lms.multimedia.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.multimedia.dto.request.VideoWebhookRequest;
import com.lms.multimedia.dto.response.VideoResponse;
import com.lms.multimedia.dto.response.VideoDetailResponse;
import com.lms.multimedia.entity.enums.VideoStatus;
import com.lms.multimedia.service.VideoService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class VideoController {

    private final VideoService videoService;

    /**
     * Webhook endpoint to receive video metadata from Express.js
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<VideoResponse>> handleWebhook(
            @Valid @RequestBody VideoWebhookRequest request) {
        log.info("Webhook received for video: {}", request.getCode());
        VideoResponse response = videoService.handleWebhook(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all videos with optional filters
     * 
     * @param status         Optional status filter
     * @param studySetId     Optional studySetId filter
     * @param includeDeleted Include deleted videos (admin only)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoResponse>>> getAllVideos(
            @RequestParam(required = false) VideoStatus status,
            @RequestParam(required = false) String studySetId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted) {
        List<VideoResponse> videos = videoService.getAllVideos(status, studySetId, includeDeleted);
        return ResponseEntity.ok(ApiResponse.ok(videos));
    }

    /**
     * Get video by code
     */
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<VideoResponse>> getVideoByCode(@PathVariable String code) {
        VideoResponse video = videoService.getVideoByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(video));
    }

    /**
     * Get detailed video information with subtitles
     * 
     * @param id Video ID
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<VideoDetailResponse>> getVideoDetails(@PathVariable String id) {
        log.info("Get video details for id: {}", id);
        VideoDetailResponse videoDetails = videoService.getVideoDetails(id);
        return ResponseEntity.ok(ApiResponse.ok(videoDetails));
    }

    /**
     * Soft delete video
     * Requires TEACHER or ADMIN role
     * 
     * @param id Video ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'TEACHER_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable String id) {
        log.info("Soft deleting video with id: {}", id);
        videoService.softDeleteVideo(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
