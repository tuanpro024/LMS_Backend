package com.lms.learningpath.dto.request;

import com.lms.learningpath.entity.enums.ModuleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateModuleRequest {

    @NotNull(message = "Module type is required")
    private ModuleType moduleType;

    @NotNull(message = "Module order is required")
    @Min(1)
    private Integer moduleOrder;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String icon;
    private String color;

    private String contentSetId; // ID của StudySet trong module khác
    private String contentFolderId;
    private String externalRefJson;

    private Integer estimatedMinutes;

    @Builder.Default
    private Boolean isRequired = true;
}
