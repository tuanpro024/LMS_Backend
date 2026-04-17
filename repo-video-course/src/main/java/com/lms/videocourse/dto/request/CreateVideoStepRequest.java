package com.lms.videocourse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVideoStepRequest {

    @NotBlank(message = "StudySet ID is required")
    private String studySetId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Step order is required")
    @Min(value = 1, message = "Step order must be >= 1")
    private Integer stepOrder;

    @Size(max = 255, message = "Icon string must not exceed 255 characters")
    private String icon;

    @Size(max = 50, message = "Color string must not exceed 50 characters")
    private String color;

    @Min(value = 0, message = "Estimated minutes must be >= 0")
    private Integer estimatedMinutes;

    private Boolean isRequired = true;
}
