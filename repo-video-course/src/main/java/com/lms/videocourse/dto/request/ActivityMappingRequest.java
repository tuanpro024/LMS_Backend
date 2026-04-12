package com.lms.videocourse.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for creating/updating an activity-module mapping.
 */
@Data
@NoArgsConstructor
public class ActivityMappingRequest {
    private String targetModuleType;
    private String targetContentSetId;
    private String targetContentFolderId;
    private String targetPackageId;
    private String targetModuleTitle;
    private String targetModuleDescription;
    private boolean isRequired;
}
