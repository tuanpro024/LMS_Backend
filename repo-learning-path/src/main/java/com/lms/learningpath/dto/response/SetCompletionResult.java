package com.lms.learningpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetCompletionResult {

    private String studySetId;
    private String title;
    private Integer finalScore;
    private Integer totalExp;
    private Instant completedAt;
    private List<String> completedModuleIds;
    private List<String> skippedModuleIds;
}