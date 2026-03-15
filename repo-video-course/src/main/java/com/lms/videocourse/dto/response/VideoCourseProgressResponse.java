package com.lms.videocourse.dto.response;

import com.lms.videocourse.entity.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VideoCourseProgressResponse {
    private String id;
    private String studySetId;
    private ProgressStatus status;
    private Integer completedSteps;
    private Integer totalSteps;
    private Double progressPercentage;
    private String currentStepId;
    private Instant firstStartedAt;
    private Instant completedAt;
}
