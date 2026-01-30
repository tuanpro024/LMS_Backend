package com.lms.learningpath.dto.request;

import com.lms.learningpath.entity.enums.ModuleType;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateModuleRequest {

    @Min(value = 1, message = "Order index must be >= 1")
    private Integer orderIndex;

    private ModuleType type;

    private String title;

    private String subtitle;

    private String icon;

    private String color;

    private String description;

    private String contentSetId;

    private String externalRefJson;

    @Min(value = 1, message = "Estimated minutes must be >= 1")
    private Integer estimatedMinutes;

    private Boolean isRequired;
}