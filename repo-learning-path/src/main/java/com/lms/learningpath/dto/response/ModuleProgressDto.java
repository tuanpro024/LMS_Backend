package com.lms.learningpath.dto.response;

import com.lms.learningpath.entity.enums.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for module progress tracking (step modules).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleProgressDto {
    private String id;
    private String stepModuleId; // Changed from studySetModuleId
    private String stepId; // Changed from studySetId
    private ProgressStatus status;
    private Integer completedItems;
    private Integer totalItems;
    private Double progressPercentage;
    private Integer score;
    private Integer totalAttempts;
    private Integer studyTimeSeconds;
    private Instant firstStartedAt;
    private Instant lastAttemptAt;
    private Instant startedAt;
    private Instant completedAt;
}
