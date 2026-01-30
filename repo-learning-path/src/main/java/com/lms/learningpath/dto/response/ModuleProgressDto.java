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
public class ModuleProgressDto {
    private String id;
    private String studySetModuleId;
    private String studySetId;
    private ProgressStatus status;
    private Integer completedItems;
    private Integer totalItems;
    private Double progressPercentage;
    private Integer score;
    private Integer studyTimeSeconds;
    private Instant startedAt;
    private Instant completedAt;
}
