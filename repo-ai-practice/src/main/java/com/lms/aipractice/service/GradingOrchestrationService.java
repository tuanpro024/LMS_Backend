package com.lms.aipractice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.adapter.AsrHskClient;
import com.lms.aipractice.adapter.HskApiClient;
import com.lms.aipractice.adapter.mapper.SpeakingRequestMapper;
import com.lms.aipractice.adapter.mapper.WritingRequestMapper;
import com.lms.aipractice.adapter.normalizer.GradingResultNormalizer;
import com.lms.aipractice.entity.AiGradingJob;
import com.lms.aipractice.entity.AiGradingResult;
import com.lms.aipractice.entity.AiPracticeAnswer;
import com.lms.aipractice.entity.AiPracticeItem;
import com.lms.aipractice.entity.enums.AiItemSubtype;
import com.lms.aipractice.entity.enums.GradingJobStatus;
import com.lms.aipractice.event.AiPracticeProgressUpdatedEvent;
import com.lms.aipractice.repository.AiGradingJobRepository;
import com.lms.aipractice.repository.AiGradingResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * Orchestrates the grading workflow for one answer:
 * 1. Detect provider from questionSubtype
 * 2. Build request payload via type-specific mapper
 * 3. Call the correct AI client
 * 4. For HSK_API (async): save job with providerJobId → polling picks it up
 * 5. For ASR_HSK (sync): save result immediately
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GradingOrchestrationService {

    static final String PROVIDER_HSK_API = "HSK_API";
    static final String PROVIDER_ASR_HSK = "ASR_HSK";

    private final HskApiClient hskApiClient;
    private final AsrHskClient asrHskClient;
    private final WritingRequestMapper writingMapper;
    private final SpeakingRequestMapper speakingMapper;
    private final GradingResultNormalizer normalizer;
    private final AiGradingJobRepository jobRepository;
    private final AiGradingResultRepository resultRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Dispatches grading for a single answer. Creates and saves the AiGradingJob.
     * 
     * @return saved AiGradingJob (PENDING for HSK_API, COMPLETED for ASR_HSK)
     */
    public AiGradingJob dispatchGrading(AiPracticeItem item, AiPracticeAnswer answer) {
        AiItemSubtype subtype = item.getQuestionSubtype();
        log.info("Dispatching grading: answerId={} subtype={}", answer.getId(), subtype);

        if (subtype == AiItemSubtype.AUDIO_COMPARE) {
            return dispatchAudioCompare(item, answer);
        } else if (subtype.name().startsWith("SPEAKING_")) {
            return dispatchSpeaking(item, answer);
        } else {
            return dispatchWriting(item, answer);
        }
    }

    // ── Writing (HSK_API v2 async, fallback v1 sync) ───────────────────

    private AiGradingJob dispatchWriting(AiPracticeItem item, AiPracticeAnswer answer) {
        Map<String, Object> payload = writingMapper.buildPayload(item, answer);
        String requestJson = toJson(payload);

        AiGradingJob job = AiGradingJob.builder()
                .attemptId(answer.getAttemptId())
                .answerId(answer.getId())
                .provider(PROVIDER_HSK_API)
                .status(GradingJobStatus.PENDING)
                .requestPayloadJson(requestJson)
                .build();

        try {
            String providerJobId = hskApiClient.submitWritingJob(payload);
            job.setProviderJobId(providerJobId);
            job.setStatus(GradingJobStatus.PROCESSING);
            log.info("Writing job submitted: providerJobId={}", providerJobId);
        } catch (Exception e) {
            if (shouldFallbackWritingToV1(e)) {
                try {
                    String rawResponse = hskApiClient.submitWritingSyncV1(payload);
                    job.setResponsePayloadJson(rawResponse);
                    job.setStatus(GradingJobStatus.COMPLETED);
                    job.setCompletedAt(Instant.now());
                    job.setErrorMessage(null);
                    job.setProviderJobId(null);

                    AiGradingJob savedJob = jobRepository.save(job);
                    AiGradingResult result = normalizer.normalizeWriting(savedJob.getId(), rawResponse);
                    resultRepository.save(result);

                    eventPublisher.publishEvent(
                            AiPracticeProgressUpdatedEvent.builder()
                                    .attemptId(answer.getAttemptId())
                                    .occurredAt(java.time.Instant.now())
                                    .build());

                    log.info("Writing graded via v1 sync fallback: jobId={}", savedJob.getId());
                    return savedJob;
                } catch (Exception fallbackEx) {
                    job.setStatus(GradingJobStatus.FAILED);
                    job.setErrorMessage(fallbackEx.getMessage());
                    log.error("Fallback writing v1 failed for answer {}: {}", answer.getId(), fallbackEx.getMessage());
                }
            } else {
                job.setStatus(GradingJobStatus.FAILED);
                job.setErrorMessage(e.getMessage());
                log.error("Failed to submit writing job for answer {}: {}", answer.getId(), e.getMessage());
            }
        }

        return jobRepository.save(job);
    }

    // ── Speaking (HSK_API async) ───────────────────────────────────────

    private AiGradingJob dispatchSpeaking(AiPracticeItem item, AiPracticeAnswer answer) {
        Map<String, Object> payload = speakingMapper.buildPayload(item, answer);
        String requestJson = toJson(payload);

        AiGradingJob job = AiGradingJob.builder()
                .attemptId(answer.getAttemptId())
                .answerId(answer.getId())
                .provider(PROVIDER_HSK_API)
                .status(GradingJobStatus.PENDING)
                .requestPayloadJson(requestJson)
                .build();

        try {
            String providerJobId = hskApiClient.submitSpeakingJob(payload);
            job.setProviderJobId(providerJobId);
            job.setStatus(GradingJobStatus.PROCESSING);
            log.info("Speaking job submitted: providerJobId={}", providerJobId);
        } catch (Exception e) {
            if (shouldFallbackSpeakingToSync(e)) {
                try {
                    String rawResponse = hskApiClient.submitSpeakingSyncV2(payload);
                    job.setResponsePayloadJson(rawResponse);
                    job.setStatus(GradingJobStatus.COMPLETED);
                    job.setCompletedAt(Instant.now());
                    job.setErrorMessage(null);
                    job.setProviderJobId(null);

                    AiGradingJob savedJob = jobRepository.save(job);
                    AiGradingResult result = normalizer.normalizeSpeaking(savedJob.getId(), rawResponse);
                    resultRepository.save(result);

                    eventPublisher.publishEvent(
                            AiPracticeProgressUpdatedEvent.builder()
                                    .attemptId(answer.getAttemptId())
                                    .occurredAt(java.time.Instant.now())
                                    .build());

                    log.info("Speaking graded via sync fallback: jobId={}", savedJob.getId());
                    return savedJob;
                } catch (Exception fallbackEx) {
                    job.setStatus(GradingJobStatus.FAILED);
                    job.setErrorMessage(fallbackEx.getMessage());
                    log.error("Fallback speaking sync failed for answer {}: {}", answer.getId(),
                            fallbackEx.getMessage());
                }
            } else {
                job.setStatus(GradingJobStatus.FAILED);
                job.setErrorMessage(e.getMessage());
                log.error("Failed to submit speaking job for answer {}: {}", answer.getId(), e.getMessage());
            }
        }

        return jobRepository.save(job);
    }

    // ── Audio Compare (ASR_HSK sync) ───────────────────────────────────

    private AiGradingJob dispatchAudioCompare(AiPracticeItem item, AiPracticeAnswer answer) {
        String requestSummary = String.format(
                "{\"reference_text\":\"%s\",\"audio_path\":\"%s\"}",
                item.getReferenceText(), answer.getAnswerAudioPath());

        AiGradingJob job = AiGradingJob.builder()
                .attemptId(answer.getAttemptId())
                .answerId(answer.getId())
                .provider(PROVIDER_ASR_HSK)
                .status(GradingJobStatus.PROCESSING)
                .requestPayloadJson(requestSummary)
                .build();

        try {
            String rawResponse = asrHskClient.evaluate(
                    answer.getAnswerAudioPath(), item.getReferenceText());
            job.setResponsePayloadJson(rawResponse);
            job.setStatus(GradingJobStatus.COMPLETED);
            job.setCompletedAt(Instant.now());
            job = jobRepository.save(job);

            // Save normalized result immediately (sync — no polling needed)
            AiGradingResult result = normalizer.normalizeAudioCompare(job.getId(), rawResponse);
            resultRepository.save(result);
            // Sync result saved → fire progress event immediately
            eventPublisher.publishEvent(
                    AiPracticeProgressUpdatedEvent.builder()
                            .attemptId(answer.getAttemptId())
                            .occurredAt(java.time.Instant.now())
                            .build());

            log.info("Audio compare graded synchronously: jobId={}", job.getId());
        } catch (Exception e) {
            job.setStatus(GradingJobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            log.error("ASR_HSK evaluate failed for answer {}: {}", answer.getId(), e.getMessage());
            jobRepository.save(job);
        }

        return job;
    }

    // ── helper ────────────────────────────────────────────────────────

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private boolean shouldFallbackWritingToV1(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        return (message.contains("HSK_API call failed (404)") && message.contains("/api/v2/grade"))
                || message.contains("Missing job_id in HSK_API response");
    }

    private boolean shouldFallbackSpeakingToSync(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        return (message.contains("HSK_API call failed (404)") && message.contains("/api/v2/speaking/grade"))
                || message.contains("Missing job_id in HSK_API response");
    }
}
