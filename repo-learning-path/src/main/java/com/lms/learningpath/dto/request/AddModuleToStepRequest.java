package com.lms.learningpath.dto.request;

import com.lms.learningpath.entity.enums.ModuleType;
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
public class AddModuleToStepRequest {

    @NotBlank(message = "Step ID is required")
    @Size(max = 26)
    private String stepId;

    @NotNull(message = "Module type is required")
    private ModuleType moduleType;

    @NotNull(message = "Module order is required")
    @Min(value = 1, message = "Module order must be at least 1")
    private Integer moduleOrder;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    private String description;

    @NotBlank(message = "Content set ID is required")
    @Size(max = 26)
    private String contentSetId; // StudySet ID from external repo

    @Size(max = 26)
    private String contentFolderId; // Optional folder ID

    private String externalRefJson; // JSON metadata

    private Boolean isRequired;
}
