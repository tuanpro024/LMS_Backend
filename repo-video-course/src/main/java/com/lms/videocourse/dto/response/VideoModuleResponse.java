package com.lms.videocourse.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VideoModuleResponse {
    private String id;
    private String stepId;
    private Integer moduleOrder;
    private String title;
    private String description;
    private Boolean isRequired;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    // ===== Inline video content =====
    private String videoUrl;
    private String thumbnailUrl;
    private Integer duration; // seconds
    private String subtitles; // JSON string
    private String videoCode; // optional multimedia reference

    // ===== Practice module link =====
    private String moduleType; // FLASHCARD, QUIZ, WRITING, KANJI_ORIGIN, etc.
    private String contentSetId; // target content ID in other services

    // ===== Watch progress (populated when authenticated) =====
    private VideoWatchProgressResponse watchProgress;
}
