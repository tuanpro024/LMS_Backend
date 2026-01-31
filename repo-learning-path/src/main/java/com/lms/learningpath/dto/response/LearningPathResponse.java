package com.lms.learningpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathResponse {

    private String id;
    private String studySetId;
    private String title;
    private String description;
    private String thumbnail;
    private Integer displayOrder;
    private Integer estimatedHours;
    private String level;
    private Boolean isActive;
    private String createdBy;
    private Instant createdDate;
    private Instant lastModifiedDate;

    // Additional fields
    private Integer totalSteps;
    private Integer completedSteps; // For specific user
    private Double progressPercentage; // For specific user
}
