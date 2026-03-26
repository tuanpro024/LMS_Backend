package com.lms.videocourse.dto.response;

import com.lms.videocourse.entity.enums.ModuleType;
import com.lms.videocourse.entity.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VideoPracticeModuleProgressResponse {
    private String id;
    private String videoModuleId;
    private String stepId;
    private String studySetId;
    private ModuleType moduleType;
    private String contentSetId;
    private ProgressStatus status;
    private double progressPercentage;
    private Instant firstStartedAt;
    private Instant completedAt;
}
