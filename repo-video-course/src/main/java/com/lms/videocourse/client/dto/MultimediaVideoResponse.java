package com.lms.videocourse.client.dto;

import lombok.Data;

/**
 * Response DTO from repo-multimedia for video metadata.
 * Used to auto-populate VideoModule fields when videoCode is provided.
 */
@Data
public class MultimediaVideoResponse {
    private String code;
    private String name;
    private String description;
    private String status; // READY, PROCESSING, etc.
    private Integer duration; // Duration in seconds
    private String thumbnailPath; // MinIO thumbnail path
    private String playlistUrl; // HLS playlist URL
    private Boolean hasSubtitle;
}
