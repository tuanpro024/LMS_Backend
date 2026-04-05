package com.lms.aipractice.dto.response;

import com.lms.aipractice.entity.enums.GradingJobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class GradingResultResponse {
    private String jobId;
    private String providerJobId;
    private String errorMessage;

    private String answerId;
    private String itemId;
    private GradingJobStatus jobStatus;

    // Populated when job is COMPLETED
    private Double normalizedScore;
    private Double normalizedMaxScore;
    private String normalizedLevel;
    private String deductionsJson;
    private String feedbackText;
    private String transcriptText;
    private String analyticsJson;

    private Instant completedAt;
}
