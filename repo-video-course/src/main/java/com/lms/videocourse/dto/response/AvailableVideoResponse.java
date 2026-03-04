package com.lms.videocourse.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for available videos from repo-multimedia.
 * Used by Admin/Teacher when selecting a video to embed in a VideoModule.
 */
@Data
@Builder
public class AvailableVideoResponse {

    /** Unique video code used as reference in VideoModule.videoCode */
    private String videoCode;

    private String title;
    private String description;

    /** HLS playlist URL */
    private String videoUrl;

    private String thumbnailUrl;

    /** Duration in seconds */
    private Integer duration;

    /** Source repo */
    private String repoName;
}
