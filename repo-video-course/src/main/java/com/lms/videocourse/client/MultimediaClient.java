package com.lms.videocourse.client;

import com.lms.common.dto.ApiResponse;
import com.lms.videocourse.client.dto.MultimediaVideoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for repo-multimedia service.
 * 1. Optionally auto-populates VideoModule inline metadata via videoCode.
 * 2. Lists all available videos for Admin/Teacher selection.
 */
@FeignClient(name = "repo-multimedia")
public interface MultimediaClient {

    /**
     * Get video metadata by code from repo-multimedia.
     * Used to auto-populate VideoModule fields: videoUrl, thumbnailUrl, duration.
     */
    @GetMapping("/api/videos/code/{code}")
    MultimediaVideoResponse getVideoByCode(@PathVariable("code") String code);

    /**
     * List all videos from repo-multimedia, optionally filtered by title query.
     * Used by AvailableVideoService.
     */
    @GetMapping("/api/videos")
    ApiResponse<List<MultimediaVideoResponse>> getAllVideos(
            @RequestParam(value = "q", required = false) String query);
}
