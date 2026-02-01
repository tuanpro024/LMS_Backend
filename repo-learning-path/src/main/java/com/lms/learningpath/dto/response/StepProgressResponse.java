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
public class StepProgressResponse {

    private String id;
    private String userId;
    private String stepId;
    private String learningPathId;
    private ProgressStatus status;
    private Integer completedModules;
    private Integer totalModules;
    private Integer requiredCompletedModules;
    private Integer totalRequiredModules;
    private Double progressPercentage;
    private Boolean canUnlockNext;
    private Instant firstStartedAt;
    private Instant completedAt;
}
