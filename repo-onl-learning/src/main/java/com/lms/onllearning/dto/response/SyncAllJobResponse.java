package com.lms.onllearning.dto.response;

import java.time.Instant;

public record SyncAllJobResponse(
        String jobId,
        String status,
        String currentStep,
        int progressPercent,
        String message,
        Instant startedAt,
        Instant finishedAt) {
}
