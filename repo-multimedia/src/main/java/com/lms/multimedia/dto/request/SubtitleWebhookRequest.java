package com.lms.multimedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubtitleWebhookRequest {

    @NotBlank(message = "Video code is required")
    private String videoCode;

    @NotBlank(message = "Subtitle name is required")
    private String subtitleName;

    @NotBlank(message = "File path is required")
    private String filePath;

    private Integer segments; // From stats
    private Integer words; // From stats

    private String status; // "completed" or "failed"
}
