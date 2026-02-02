package com.lms.multimedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubtitleUploadRequest {

    @NotBlank(message = "Video ID is required")
    private String videoId;

    @NotBlank(message = "Subtitle name is required")
    private String name;

    // Note: MultipartFile will be passed as @RequestParam in controller
}
