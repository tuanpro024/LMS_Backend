package com.lms.learningpath.dto.request;

import com.lms.learningpath.entity.enums.ModuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a module to a learning section.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddModuleToSectionRequest {

    @NotNull(message = "Module type is required")
    private ModuleType moduleType;

    @NotBlank(message = "Study set ID is required")
    private String studySetId;

    @NotNull(message = "Module order is required")
    private Integer moduleOrder;

    private String displayTitle; // Optional: Override the StudySet's title

    @Builder.Default
    private Boolean isRequired = true; // Default to required

    private String moduleDescription;
}
