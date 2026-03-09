package com.lms.videocourse.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VideoCourseResponse {
    private String id;
    private String studySetId;
    private String title;
    private String description;
    private String thumbnail;
    private Integer estimatedHours;
    private Boolean isActive;
    private String createdBy;
    private Integer contentIndex;
    private Instant createdAt;
    private Instant updatedAt;

    // Progress info (populated when authenticated)
    private VideoCourseProgressResponse progress;
}
