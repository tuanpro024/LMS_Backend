package com.lms.learningpath.dto.response;

import com.lms.learningpath.dto.external.StudySetDto;
import com.lms.learningpath.entity.enums.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySetModuleResponse {
    private String id;
    private String studySetId;
    private ModuleType moduleType;
    private Integer moduleOrder;
    private String title;
    private String description;
    private String icon;
    private String color;
    private String contentSetId;
    private Integer estimatedMinutes;
    private Boolean isRequired;

    // Enriched data
    private StudySetDto contentSetDetails; // Chi tiết của content set

    // User progress
    private ModuleProgressDto userProgress;
}
