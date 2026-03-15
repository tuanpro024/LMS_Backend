package com.lms.videocourse.dto.response;

import com.lms.videocourse.entity.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VideoStepProgressResponse {
    private String id;
    private String stepId;
    private String studySetId;
    private ProgressStatus status;
    private Integer completedModules;
    private Integer totalModules;
    private Integer requiredCompletedModules;
    private Integer totalRequiredModules;
    private Double progressPercentage;
    private Instant firstStartedAt;
    private Instant completedAt;
}
