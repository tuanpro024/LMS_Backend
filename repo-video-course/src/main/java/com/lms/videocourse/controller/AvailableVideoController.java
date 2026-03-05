package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.videocourse.dto.response.AvailableVideoResponse;
import com.lms.videocourse.service.IAvailableVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for browsing available videos from repo-multimedia.
 * Used by Admin/Teacher when selecting a video to embed in a VideoModule.
 * This is the video-course-specific extension — unique to repo-video-course.
 * Only accessible to Admin and Teacher roles.
 */
@RestController
@RequestMapping("/admin/available-videos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class AvailableVideoController {

    private final IAvailableVideoService availableVideoService;

    /**
     * Get all available videos from repo-multimedia.
     * Optional query param for title search.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailableVideoResponse>>> getAllAvailableVideos(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(availableVideoService.getAllAvailableVideos(q)));
    }

    /**
     * Get a single video by its videoCode.
     * Use this to preview a specific video before embedding it into a VideoModule.
     */
    @GetMapping("/{videoCode}")
    public ResponseEntity<ApiResponse<AvailableVideoResponse>> getVideoByCode(
            @PathVariable String videoCode) {
        return ResponseEntity.ok(ApiResponse.ok(availableVideoService.getVideoByCode(videoCode)));
    }
}
