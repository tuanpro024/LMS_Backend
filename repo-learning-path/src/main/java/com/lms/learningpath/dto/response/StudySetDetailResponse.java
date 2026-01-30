package com.lms.learningpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for user's detailed StudySet information.
 * Used to display complete StudySet details when user enters a module.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySetDetailResponse {
    private String id;
    private String title;
    private String description;
    private String thumbnail;
    private Boolean isPrivate;
    private Integer estimatedMinutes;

    // Module information
    private String moduleType;
    private Long itemCount;

    // User progress
    private Long learnedCount;
    private Long unlearnedCount;
    private Double completionPercentage;

    // Items (texts, lessons, etc. - structure varies by module)
    private List<Object> items; // Generic list, actual type depends on module

    // Metadata
    private LocalDateTime createdAt;
}
