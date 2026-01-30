package com.lms.learningpath.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing unlock rule.
 * All fields are optional to support partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUnlockRuleRequest {

    private String requiredStudySetId; // StudySet phải hoàn thành trước

    private Boolean requirePreviousInFolder; // Phải hoàn thành StudySet trước trong cùng Folder?

    private Boolean requireAllRequiredModules; // Phải hoàn thành tất cả module bắt buộc?

    private Boolean isActive; // Active status
}
