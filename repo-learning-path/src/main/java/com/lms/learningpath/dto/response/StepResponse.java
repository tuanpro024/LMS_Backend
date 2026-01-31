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
public class StepResponse {

    private String id;
    private String learningPathId;
    private String title;
    private String description;
    private Integer stepOrder;
    private String icon;
    private String color;
    private Integer estimatedMinutes;
    private Boolean isRequired;
    private Boolean isActive;
    private Instant createdDate;
    private Instant lastModifiedDate;

    // Additional fields
    private Integer totalModules;
    private Integer completedModules; // For specific user
    private Boolean isUnlocked; // For specific user
    private Double progressPercentage; // For specific user
    private List<StepModuleResponse> modules;
}
