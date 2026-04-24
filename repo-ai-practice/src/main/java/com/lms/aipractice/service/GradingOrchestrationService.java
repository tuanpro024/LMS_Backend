package com.lms.aipractice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Orchestrates the grading workflow for one answer:
 * 1. Detect provider from questionSubtype
 * 2. Build request payload via type-specific mapper
 * 3. Call the correct AI client
 * 4. For HSK_API async routes: save job with providerJobId -> polling picks it
 * up
 * 5. For sync routes (e.g. speaking/grade/sync): save result immediately
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GradingOrchestrationService {

    static final String PROVIDER_HSK_API = "HSK_API";
    private static final int MAX_TEXT_COLUMN_LENGTH = 60_000;

    private final HskApiClient hskApiClient;
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
     * @return saved AiGradingJob
     */
    public AiGradingJob dispatchGrading(AiPracticeItem item, AiPracticeAnswer answer) {
        AiItemSubtype subtype = item.getQuestionSubtype();
        log.info("Dispatching grading: answerId={} subtype={}", answer.getId(), subtype);

        try {
            if (subtype == null) {
                throw new IllegalArgumentException("questionSubtype is null for itemId=" + item.getId());
            }

            if (subtype == AiItemSubtype.AUDIO_COMPARE) {
                return dispatchAudioCompare(item, answer);
            } else if (subtype.name().startsWith("SPEAKING_")) {
                return dispatchSpeaking(item, answer);
            } else {
                return dispatchWriting(item, answer);
            }
        } catch (Exception ex) {
            log.error("Dispatch grading failed before job persisted: answerId={} itemId={} subtype={} err={}",
                    answer.getId(), item.getId(), subtype, ex.getMessage(), ex);
            return jobRepository.save(buildDispatchFailureJob(item, answer, ex));
        }
    }

    private AiGradingJob buildDispatchFailureJob(AiPracticeItem item, AiPracticeAnswer answer, Exception ex) {
        Map<String, Object> debugPayload = new HashMap<>();
        debugPayload.put("itemId", item.getId());
        debugPayload.put("questionSubtype",
                item.getQuestionSubtype() != null ? item.getQuestionSubtype().name() : null);
        debugPayload.put("answerText", answer.getAnswerText());
        debugPayload.put("answerAudioPath", answer.getAnswerAudioPath());

        return AiGradingJob.builder()
                .attemptId(answer.getAttemptId())
                .answerId(answer.getId())
                .provider(PROVIDER_HSK_API)
                .status(GradingJobStatus.FAILED)
                .requestPayloadJson(toJson(debugPayload))
                .errorMessage(ex.getMessage())
                .build();
    }

    // -- Writing (HSK_API v2 async, fallback v1 sync) ------------------

    private AiGradingJob dispatchWriting(AiPracticeItem item, AiPracticeAnswer answer) {
        Map<String, Object> payload = writingMapper.buildPayload(item, answer);
        String requestJson = toCompactRequestJson(payload);

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

    // -- Speaking (HSK_API v2 sync, fallback async) --------------------

    private AiGradingJob dispatchSpeaking(AiPracticeItem item, AiPracticeAnswer answer) {
        Map<String, Object> payload = speakingMapper.buildPayload(item, answer);
        String requestJson = toCompactRequestJson(payload);

        AiGradingJob job = AiGradingJob.builder()
                .attemptId(answer.getAttemptId())
                .answerId(answer.getId())
                .provider(PROVIDER_HSK_API)
                .status(GradingJobStatus.PROCESSING)
                .requestPayloadJson(requestJson)
                .build();

        try {
            String providerJobId = hskApiClient.submitSpeakingJob(payload);
            job.setProviderJobId(providerJobId);
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

                    log.info("Speaking graded via fallback sync endpoint: jobId={}", savedJob.getId());
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

    // -- Audio Compare (HSK_API sync) ----------------------------------
    // The /api/v2/audio/compare/upload endpoint returns the full grading result
    // synchronously (like Postman — ~2-3s). No job polling is needed.

    private AiGradingJob dispatchAudioCompare(AiPracticeItem item, AiPracticeAnswer answer) {
        String audioPath = answer.getAnswerAudioPath();
        boolean useMultipart = audioPath != null && !audioPath.isBlank() && Files.exists(Path.of(audioPath));

        Map<String, Object> debugPayload = new HashMap<>();
        debugPayload.put("reference_text", item.getReferenceText());
        debugPayload.put("student_audio_path", audioPath);
        debugPayload.put("upload_mode", useMultipart);
        String requestJson = toCompactRequestJson(debugPayload);

        AiGradingJob job = AiGradingJob.builder()
                .attemptId(answer.getAttemptId())
                .answerId(answer.getId())
                .provider(PROVIDER_HSK_API)
                .status(GradingJobStatus.PENDING)
                .requestPayloadJson(requestJson)
                .build();

        try {
            String rawResponse;
            if (useMultipart) {
                org.springframework.util.MultiValueMap<String, Object> parts = new org.springframework.util.LinkedMultiValueMap<>();
                parts.add("reference_text", item.getReferenceText());
                parts.add("student_audio", new org.springframework.core.io.FileSystemResource(Path.of(audioPath)));
                rawResponse = hskApiClient.submitAudioCompareSyncUpload(parts);
            } else {
                Map<String, Object> payload = buildAudioComparePayload(item, answer);
                rawResponse = hskApiClient.submitAudioCompareSyncBase64(payload);
            }

            job.setResponsePayloadJson(rawResponse);
            job.setStatus(GradingJobStatus.COMPLETED);
            job.setCompletedAt(Instant.now());
            job.setProviderJobId(null); // sync — no provider job id

            AiGradingJob savedJob = jobRepository.save(job);
            AiGradingResult result = normalizer.normalizeAudioCompare(savedJob.getId(), rawResponse);
            resultRepository.save(result);

            eventPublisher.publishEvent(
                    AiPracticeProgressUpdatedEvent.builder()
                            .attemptId(answer.getAttemptId())
                            .occurredAt(Instant.now())
                            .build());

            log.info("Audio compare graded synchronously via {}: jobId={}", useMultipart ? "upload" : "base64", savedJob.getId());
            return savedJob;
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            job.setStatus(GradingJobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            log.error("Audio compare grading failed for answer {}: {}", answer.getId(), errorMessage, e);
            return jobRepository.save(job);
        }
    }

    // -- helper --------------------------------------------------------

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String toCompactRequestJson(Map<String, Object> payload) {
        Map<String, Object> compactPayload = new HashMap<>(payload);
        compactBase64Field(compactPayload, "student_audio_base64");
        compactBase64Field(compactPayload, "reference_audio_base64");

        String json = toJson(compactPayload);
        if (json.length() <= MAX_TEXT_COLUMN_LENGTH) {
            return json;
        }

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("truncated", true);
        fallback.put("original_length", json.length());
        fallback.put("preview", json.substring(0, Math.min(2_000, json.length())));
        return toJson(fallback);
    }

    private void compactBase64Field(Map<String, Object> payload, String fieldName) {
        Object raw = payload.get(fieldName);
        if (!(raw instanceof String base64) || base64.isBlank()) {
            return;
        }

        payload.put(fieldName, "[omitted;base64;length=" + base64.length() + "]");
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

        return message.contains("HSK_API call failed (404)") && message.contains("/api/v2/speaking/grade");
    }

    private Map<String, Object> buildAudioComparePayload(AiPracticeItem item, AiPracticeAnswer answer) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reference_text", item.getReferenceText());
        payload.put("student_audio_path", null);

        String audioPath = answer.getAnswerAudioPath();
        if (audioPath == null || audioPath.isBlank()) {
            payload.put("student_audio_base64", null);
            return payload;
        }

        try {
            byte[] audioBytes = Files.readAllBytes(Path.of(audioPath));
            payload.put("student_audio_base64", Base64.getEncoder().encodeToString(audioBytes));
            return payload;
        } catch (Exception e) {
            throw new RuntimeException("Cannot read audio file: " + audioPath, e);
        }
    }
}