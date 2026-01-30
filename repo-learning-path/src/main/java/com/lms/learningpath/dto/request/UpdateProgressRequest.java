package com.lms.learningpath.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressRequest {

    @Min(0)
    private Integer completedItems;

    @Min(0)
    private Integer studyTimeSeconds;

    private Integer score;

    private String metadata; // JSON
}
