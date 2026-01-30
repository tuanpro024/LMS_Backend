package com.lms.learningpath.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new StudySet unlock rule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUnlockRuleRequest {

    @NotBlank(message = "Study set ID is required")
    private String studySetId; // StudySet cần unlock

    private String requiredStudySetId; // StudySet phải hoàn thành trước (null = không yêu cầu)

    @Builder.Default
    private Boolean requirePreviousInFolder = true; // Phải hoàn thành StudySet trước trong cùng Folder?

    @Builder.Default
    private Boolean requireAllRequiredModules = true; // Phải hoàn thành tất cả module bắt buộc?
}
