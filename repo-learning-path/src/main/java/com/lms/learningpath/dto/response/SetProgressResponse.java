package com.lms.learningpath.dto.response;

import com.lms.learningpath.entity.enums.SetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetProgressResponse {

    private String studySetId;
    private SetStatus status;
    private Integer completedModules;
    private Integer totalModules;
    private Integer requiredCompleted;
    private Integer requiredTotal;
    private Integer bestScore;
    private Integer earnedExp;
    private Boolean canCompleteSet;
    private Instant completedAt;
    private Instant lastStudiedAt;
}