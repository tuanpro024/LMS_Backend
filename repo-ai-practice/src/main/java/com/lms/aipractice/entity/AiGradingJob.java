package com.lms.aipractice.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.aipractice.entity.enums.GradingJobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tracks one AI grading job submitted to either HSK_API or ASR_HSK.
 * - HSK_API: async job — stores providerJobId for polling
 * - ASR_HSK: sync call — completed immediately after response
 */
@Entity
@Table(name = "ai_grading_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiGradingJob extends BaseEntity {

    @Column(name = "attempt_id", nullable = false, length = 26)
    private String attemptId;

    @Column(name = "answer_id", nullable = false, length = 26)
    private String answerId;

    /**
     * Which AI service was called: HSK_API or ASR_HSK.
     * Used by GradingPollingService to route retries correctly.
     */
    @Column(nullable = false, length = 20)
    private String provider;

    /**
     * job_id returned by HSK_API async endpoint.
     * Null for ASR_HSK (sync) calls.
     */
    @Column(name = "provider_job_id", length = 100)
    private String providerJobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GradingJobStatus status = GradingJobStatus.PENDING;

    /** Full JSON payload sent to AI service (for audit/debug) */
    @Column(name = "request_payload_json", columnDefinition = "TEXT")
    private String requestPayloadJson;

    /** Full JSON response from AI service (raw, before normalization) */
    @Column(name = "response_payload_json", columnDefinition = "TEXT")
    private String responsePayloadJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;
}
