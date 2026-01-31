package com.lms.learningpath.dto.response;

import com.lms.learningpath.entity.enums.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathProgressResponse {

    private String id;
    private String userId;
    private String learningPathId;
    private String studySetId;
    private ProgressStatus status;
    private Integer completedSteps;
    private Integer totalSteps;
    private String currentStepId;
    private Double progressPercentage;
    private Instant firstStartedAt;
    private Instant completedAt;
}
