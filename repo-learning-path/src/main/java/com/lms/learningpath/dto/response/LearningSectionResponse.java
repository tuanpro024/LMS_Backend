package com.lms.learningpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for learning section (Folder) with unlock status and modules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSectionResponse {
    private String id;
    private String name;
    private String description;
    private String thumbnail;
    private String packageId;
    private Integer sectionOrder;

    // Progress and unlock information
    private Boolean isLocked;
    private Boolean isCompleted;
    private Double completionPercentage;
    private String prerequisiteSectionId;

    // Modules within this section
    private List<SectionModuleResponse> modules;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
