package com.lms.videocourse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateVideoModuleRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    @Min(value = 1, message = "Module order must be >= 1")
    private Integer moduleOrder;
    @Size(max = 2048, message = "Video URL must not exceed 2048 characters")
    private String videoUrl;
    @Size(max = 2048, message = "Thumbnail URL must not exceed 2048 characters")
    private String thumbnailUrl;
    @Min(value = 0, message = "Duration must be >= 0")
    private Integer duration;
    private String subtitles;
    @Size(max = 255, message = "Video Code must not exceed 255 characters")
    private String videoCode;
    @Size(max = 50, message = "Module Type must not exceed 50 characters")
    private String moduleType;
    @Size(max = 255, message = "Content Set ID must not exceed 255 characters")
    private String contentSetId;
    private Boolean isRequired;
    private Boolean isActive;
}
