package com.lms.learningpath.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteModuleRequest {

    @Min(0)
    @Max(100)
    private Integer score;

    @Min(0)
    private Integer totalStudyTimeSeconds;

    /**
     * For QUIZ modules, map from repo-quiz submit result `passed`.
     * If false, module will remain IN_PROGRESS until user passes.
     */
    private Boolean passed;

    private String metadata; // JSON
}
