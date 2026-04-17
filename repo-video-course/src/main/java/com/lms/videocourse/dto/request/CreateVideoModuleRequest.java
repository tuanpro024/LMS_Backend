package com.lms.videocourse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVideoModuleRequest {

    @NotBlank(message = "Step ID is required")
    private String stepId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Module order is required")
    @Min(value = 1, message = "Module order must be >= 1")
    private Integer moduleOrder;

    // ===== Inline video content =====

    @Size(max = 2048, message = "Video URL must not exceed 2048 characters")
    private String videoUrl; // Direct URL or HLS playlist

    @Size(max = 2048, message = "Thumbnail URL must not exceed 2048 characters")
    private String thumbnailUrl;

    @Min(value = 0, message = "Duration must be >= 0")
    private Integer duration; // In seconds

    private String subtitles; // Subtitle data as JSON string

    // ===== Optional: link to repo-multimedia =====
    // If set, will auto-populate videoUrl, thumbnailUrl, duration from multimedia
    // service
    @Size(max = 255, message = "Video Code must not exceed 255 characters")
    private String videoCode;

    // ===== Practice Module Link (for non-video modules) =====
    @Size(max = 50, message = "Module Type must not exceed 50 characters")
    private String moduleType; // FLASHCARD, QUIZ, WRITING, KANJI_ORIGIN, etc.
    @Size(max = 255, message = "Content Set ID must not exceed 255 characters")
    private String contentSetId; // target study set / content ID in other services

    private Boolean isRequired = true;
}
