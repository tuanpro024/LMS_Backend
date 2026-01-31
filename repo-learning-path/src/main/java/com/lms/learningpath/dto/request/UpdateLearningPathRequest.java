package com.lms.learningpath.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLearningPathRequest {

    @Size(max = 255)
    private String title;

    private String description;

    private String thumbnail;

    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;

    private Integer estimatedHours;

    @Size(max = 20)
    private String level;

    private Boolean isActive;
}
