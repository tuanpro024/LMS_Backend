package com.lms.videocourse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for activity-module mapping.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMappingDTO {
    private String id;
    private String syllabusActivityId;
    private String targetModuleType;
    private String targetContentSetId;
    private String targetContentFolderId;
    private String targetPackageId;
    private String targetModuleTitle;
    private String targetModuleDescription;
    private boolean isRequired;
    private String status;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
