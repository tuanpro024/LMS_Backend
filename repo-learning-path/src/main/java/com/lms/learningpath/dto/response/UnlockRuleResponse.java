package com.lms.learningpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for StudySetUnlockRule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockRuleResponse {

    private String id;
    private String studySetId; // StudySet cần unlock
    private String requiredStudySetId; // StudySet phải hoàn thành trước
    private Boolean requirePreviousInFolder;
    private Boolean requireAllRequiredModules;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
