package com.lms.videocourse.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VideoStepResponse {
    private String id;
    private String videoCourseId;
    private String title;
    private String description;
    private Integer stepOrder;
    private String icon;
    private String color;
    private Integer estimatedMinutes;
    private Boolean isRequired;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    // Unlock status (populated when authenticated)
    private Boolean isUnlocked;
    private String lockReason;

    // Module count
    private Integer moduleCount;

    // Progress info (populated when authenticated)
    private VideoStepProgressResponse progress;
}
