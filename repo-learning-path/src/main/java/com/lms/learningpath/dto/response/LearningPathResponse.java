package com.lms.learningpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for learning path (Package) with sections and progress.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathResponse {
    private String id;
    private String name;
    private String description;
    private String thumbnail;
    private String typeName;
    private String userId;

    // Learning path specific fields
    private String level; // e.g., "HSK1", "HSK2"
    private Integer estimatedHours;
    private Boolean isPublished;

    // User progress information (if user is authenticated)
    private Double userCompletionPercentage;
    private String currentSectionId;

    // Sections within this learning path
    private List<LearningSectionResponse> sections;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
