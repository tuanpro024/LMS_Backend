package com.lms.videocourse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for generation readiness status of a course.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationStatusDTO {
    private String cmsCourseId;
    private String courseName;
    private int totalActivities;
    private int mappedActivities;
    private int unmappedActivities;
    private boolean canGenerate;
    private List<UnmappedActivityInfo> unmappedList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnmappedActivityInfo {
        private String activityId;
        private String activityName;
        private String stepName;
        private String studySetName;
        private String folderName;
    }
}
