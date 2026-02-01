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

    private String metadata; // JSON
}
