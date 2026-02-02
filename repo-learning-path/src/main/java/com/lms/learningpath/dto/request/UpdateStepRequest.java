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
public class UpdateStepRequest {

    @Size(max = 255)
    private String title;

    private String description;

    @Min(value = 1, message = "Step order must be at least 1")
    private Integer stepOrder;

    @Size(max = 255)
    private String icon;

    @Size(max = 20)
    private String color;

    private Integer estimatedMinutes;

    private Boolean isRequired;

    private Boolean isActive;
}
