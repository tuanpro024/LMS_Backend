package com.lms.multimedia.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.multimedia.dto.request.VideoWebhookRequest;
import com.lms.multimedia.dto.response.VideoResponse;
import com.lms.multimedia.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
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
     * Get all videos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoResponse>>> getAllVideos() {
        List<VideoResponse> videos = videoService.getAllVideos();
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
     * Get videos by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<VideoResponse>>> getVideosByUserId(@PathVariable String userId) {
        List<VideoResponse> videos = videoService.getVideosByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(videos));
    }
}
