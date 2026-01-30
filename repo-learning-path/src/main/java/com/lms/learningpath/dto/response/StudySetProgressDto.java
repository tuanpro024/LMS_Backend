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
public class StudySetProgressDto {
    private String id;
    private String studySetId;
    private String studySetTitle;
    private String folderId;
    private ProgressStatus status;
    private Integer completedModules;
    private Integer totalModules;
    private Integer requiredCompletedModules;
    private Integer totalRequiredModules;
    private Double progressPercentage;
    private Boolean isLocked;
    private String lockReason; // "Requires HSK1 completion"
    private Instant firstStartedAt;
    private Instant completedAt;
}
