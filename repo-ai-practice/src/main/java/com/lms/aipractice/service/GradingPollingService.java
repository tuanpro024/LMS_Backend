package com.lms.aipractice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.adapter.normalizer.GradingResultNormalizer;
import com.lms.aipractice.entity.AiGradingJob;
import com.lms.aipractice.entity.AiGradingResult;
import com.lms.aipractice.entity.enums.GradingJobStatus;
import com.lms.aipractice.event.AiPracticeProgressUpdatedEvent;
import com.lms.aipractice.repository.AiGradingJobRepository;
import com.lms.aipractice.repository.AiGradingResultRepository;
import com.lms.aipractice.adapter.HskApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Polls HSK_API for pending/processing writing & speaking jobs.
 * Runs every N milliseconds (configured via app.ai.polling.interval-ms).
 * Auto-retries up to max-retries; marks TIMEOUT after job-timeout-minutes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GradingPollingService {

    private final AiGradingJobRepository jobRepository;
    private final AiGradingResultRepository resultRepository;
    private final HskApiClient hskApiClient;
    private final GradingResultNormalizer normalizer;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.polling.max-retries:5}")
    private int maxRetries;

    @Value("${app.ai.polling.job-timeout-minutes:10}")
    private int jobTimeoutMinutes;

    @Scheduled(fixedDelayString = "${app.ai.polling.interval-ms:3000}")
    @Transactional
    public void pollPendingJobs() {
        List<AiGradingJob> pendingJobs = jobRepository.findByProviderAndStatusInAndDeletedFalse(
                GradingOrchestrationService.PROVIDER_HSK_API,
                List.of(GradingJobStatus.PENDING, GradingJobStatus.PROCESSING));

        for (AiGradingJob job : pendingJobs) {
            processJob(job);
        }
    }

    private void processJob(AiGradingJob job) {
        // Check timeout
        if (job.getCreatedAt().isBefore(Instant.now().minus(jobTimeoutMinutes, ChronoUnit.MINUTES))) {
            log.warn("Job {} timed out after {} minutes", job.getId(), jobTimeoutMinutes);
            job.setStatus(GradingJobStatus.TIMEOUT);
            jobRepository.save(job);
            return;
        }

        // Check max retries
        if (job.getRetryCount() >= maxRetries) {
            log.warn("Job {} exceeded max retries ({})", job.getId(), maxRetries);
            job.setStatus(GradingJobStatus.FAILED);
            job.setErrorMessage("Exceeded max retries: " + maxRetries);
            jobRepository.save(job);
            return;
        }

        if (job.getProviderJobId() == null) {
            log.warn("Job {} has no providerJobId, skipping poll", job.getId());
            return;
        }

        try {
            Optional<String> resultOpt = hskApiClient.pollJobResult(job.getProviderJobId());
            if (resultOpt.isEmpty()) {
                // Still pending/processing at provider side; wait for next poll.
                return;
            }

            String rawJson = resultOpt.get();
            JsonNode providerNode = objectMapper.readTree(rawJson);
            String providerStatus = providerNode.path("status").asText("");

            if ("failed".equalsIgnoreCase(providerStatus)) {
                job.setResponsePayloadJson(rawJson);
                job.setStatus(GradingJobStatus.FAILED);
                job.setCompletedAt(Instant.now());

                String providerError = providerNode.path("error").asText(providerNode.path("message").asText(null));
                if (providerError != null && !providerError.isBlank()) {
                    job.setErrorMessage(providerError);
                }

                jobRepository.save(job);
                log.warn("Provider reported FAILED for job {}: {}", job.getId(), job.getErrorMessage());
                return;
            }

            if (!"completed".equalsIgnoreCase(providerStatus)) {
                // Defensive fallback for any unrecognized provider statuses.
                return;
            }

            job.setResponsePayloadJson(rawJson);
            job.setStatus(GradingJobStatus.COMPLETED);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);

            // Normalize and save result
            boolean isSpeaking = rawJson.contains("final_score") || rawJson.contains("transcript");
            AiGradingResult result = isSpeaking
                    ? normalizer.normalizeSpeaking(job.getId(), rawJson)
                    : normalizer.normalizeWriting(job.getId(), rawJson);
            resultRepository.save(result);
            log.info("Job {} completed and result saved (speaking={})", job.getId(), isSpeaking);

            // Fire internal Spring event → listener aggregates score + publishes Kafka
            eventPublisher.publishEvent(
                    AiPracticeProgressUpdatedEvent.builder()
                            .attemptId(job.getAttemptId())
                            .occurredAt(java.time.Instant.now())
                            .build());

        } catch (Exception e) {
            log.error("Error polling job {}: {}", job.getId(), e.getMessage());
            job.setRetryCount(job.getRetryCount() + 1);
            job.setErrorMessage(e.getMessage());
            jobRepository.save(job);
        }
    }
}
