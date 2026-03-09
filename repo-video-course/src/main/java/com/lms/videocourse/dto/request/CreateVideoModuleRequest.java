package com.lms.videocourse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVideoModuleRequest {

    @NotBlank(message = "Step ID is required")
    private String stepId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Module order is required")
    @Min(value = 1, message = "Module order must be >= 1")
    private Integer moduleOrder;

    // ===== Inline video content =====

    private String videoUrl; // Direct URL or HLS playlist

    private String thumbnailUrl;

    private Integer duration; // In seconds

    private String subtitles; // Subtitle data as JSON string

    // ===== Optional: link to repo-multimedia =====
    // If set, will auto-populate videoUrl, thumbnailUrl, duration from multimedia
    // service
    private String videoCode;

    // ===== Practice Module Link (for non-video modules) =====
    private String moduleType; // FLASHCARD, QUIZ, WRITING, KANJI_ORIGIN, etc.
    private String contentSetId; // target study set / content ID in other services

    private Boolean isRequired = true;
}
