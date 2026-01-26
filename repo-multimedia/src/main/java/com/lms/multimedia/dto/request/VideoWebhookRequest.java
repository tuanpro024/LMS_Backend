package com.lms.multimedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VideoWebhookRequest {
    @NotBlank(message = "Video code is required")
    private String code;

    @NotBlank(message = "Video name is required")
    private String name;

    private String description;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Status is required")
    private String status; // "completed", "failed", "processing"

    private Integer duration; // Duration in seconds
    private String thumbnailPath;
    private String subtitlePath;
    private String karaokePath;
}
