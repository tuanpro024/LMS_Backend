package com.lms.aipractice.dto.response;

import com.lms.aipractice.entity.enums.GradingJobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class GradingJobResponse {
    private String id;
    private String attemptId;
    private String answerId;
    private String provider;
    private String providerJobId;
    private GradingJobStatus status;
    private String errorMessage;
    private int retryCount;
    private Instant createdAt;
    private Instant completedAt;
}
