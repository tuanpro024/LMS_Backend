package com.lms.videocourse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusTreeDTO {

    private String id;
    private String name;
    private String description;
    private String category;
    private List<FolderDTO> folders;

    // Course info (from CMS sync)
    private String courseName;
    private String courseDescription;
    private BigDecimal coursePrice;
    private String courseType;
    private Instant lastSyncedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FolderDTO {
        private String id;
        private String name;
        private String description;
        private Integer displayOrder;
        private List<StudySetDTO> studySets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudySetDTO {
        private String id;
        private String name;
        private String description;
        private Integer sessionNo;
        private List<StepDTO> steps;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepDTO {
        private String id;
        private String name;
        private String description;
        private String moduleName;
        private Integer displayOrder;
        private List<ActivityDTO> activities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDTO {
        private String id;
        private String cmsModuleId;
        private String name;
        private String videoContent;
        private String documentContent;
        private Integer displayOrder;
    }
}
