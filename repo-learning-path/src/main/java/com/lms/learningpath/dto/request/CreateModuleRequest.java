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

    @NotBlank(message = "Study Set ID is required")
    private String studySetId;

    @NotNull(message = "Order index is required")
    @Min(value = 1, message = "Order index must be >= 1")
    private Integer orderIndex;

    @NotNull(message = "Module type is required")
    private ModuleType type;

    @NotBlank(message = "Title is required")
    private String title;

    private String subtitle;

    private String icon;

    private String color;

    private String description;

    private String contentSetId;  // For FLASHCARD/WRITING type

    private String externalRefJson;  // For QUIZ/PRONUNCIATION/etc.

    @Min(value = 1, message = "Estimated minutes must be >= 1")
    private Integer estimatedMinutes;

    @Builder.Default
    private Boolean isRequired = true;
}