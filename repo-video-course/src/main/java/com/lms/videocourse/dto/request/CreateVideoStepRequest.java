package com.lms.videocourse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVideoStepRequest {

    @NotBlank(message = "StudySet ID is required")
    private String studySetId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Step order is required")
    @Min(value = 1, message = "Step order must be >= 1")
    private Integer stepOrder;

    private String icon;

    private String color;

    private Integer estimatedMinutes;

    private Boolean isRequired = true;
}
