package com.lms.learningpath.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLearningPathRequest {

    @NotBlank(message = "Study set ID is required")
    @Size(max = 26)
    private String studySetId;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    private String description;

    private String thumbnail;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;

    private Integer estimatedHours;

    @Size(max = 20)
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
}
