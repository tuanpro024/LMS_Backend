package com.lms.aipractice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.adapter.HskApiClient;
import com.lms.aipractice.adapter.normalizer.GradingResultNormalizer;
import com.lms.aipractice.dto.request.CreateAttemptRequest;
import com.lms.aipractice.dto.request.SubmitAnswerRequest;
import com.lms.aipractice.dto.response.AttemptResponse;
import com.lms.aipractice.dto.response.GradingJobResponse;
import com.lms.aipractice.dto.response.GradingResultResponse;
import com.lms.aipractice.entity.*;
import com.lms.aipractice.entity.enums.AiItemSubtype;
import com.lms.aipractice.entity.enums.AttemptStatus;
import com.lms.aipractice.entity.enums.GradingJobStatus;
import com.lms.aipractice.event.AiPracticeProgressUpdatedEvent;
import com.lms.aipractice.mapper.AiPracticeMapper;
import com.lms.aipractice.repository.*;
import com.lms.aipractice.service.AiPracticeAttemptService;
import com.lms.aipractice.service.GradingOrchestrationService;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.repository.StudySetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AiPracticeAttemptServiceImpl implements AiPracticeAttemptService {

    private final AiPracticeAttemptRepository attemptRepository;
    private final AiPracticeAnswerRepository answerRepository;
    private final AiPracticeItemRepository itemRepository;
    private final AiGradingJobRepository jobRepository;
    private final AiGradingResultRepository resultRepository;
    private final StudySetRepository studySetRepository;
    private final GradingOrchestrationService orchestrationService;
    private final AiPracticeMapper mapper;
    private final HskApiClient hskApiClient;
    private final GradingResultNormalizer normalizer;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public AttemptResponse createAttempt(CreateAttemptRequest request, String userId) {
        log.info("Creating attempt for userId={} studySetId={}", userId, request.getStudySetId());

        if (!studySetRepository.existsById(request.getStudySetId())) {
            throw new ApiException(ErrorCode.E227, "StudySet not found: " + request.getStudySetId());
        }

        AiPracticeAttempt attempt = AiPracticeAttempt.builder()
                .userId(userId)
                .studySetId(request.getStudySetId())
                .status(AttemptStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .build();

        return mapper.toAttemptResponse(attemptRepository.save(attempt));
    }

    @Override
    public AttemptResponse submitAnswer(String attemptId, SubmitAnswerRequest request, String userId) {
        AiPracticeAttempt attempt = getAttemptOwned(attemptId, userId);

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ApiException(ErrorCode.E227, "Attempt is not in progress: " + attemptId);
        }

        AiPracticeItem item = itemRepository.findByIdAndDeletedFalse(request.getItemId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Item not found: " + request.getItemId()));

        // Upsert answer
        AiPracticeAnswer answer = answerRepository
                .findByAttemptIdAndItemIdAndDeletedFalse(attemptId, item.getId())
                .orElse(AiPracticeAnswer.builder()
                        .attemptId(attemptId)
                        .itemId(item.getId())
                        .build());

        answer.setAnswerText(request.getAnswerText());
        answer.setAnswerAudioPath(request.getAnswerAudioPath());
        answer.setSubmittedAt(Instant.now());
        answer = answerRepository.save(answer);

        // Dispatch grading asynchronously
        orchestrationService.dispatchGrading(item, answer);

        return mapper.toAttemptResponse(attempt);
    }

    @Override
    public AttemptResponse submitAttempt(String attemptId, String userId) {
        AiPracticeAttempt attempt = getAttemptOwned(attemptId, userId);
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(Instant.now());
        return mapper.toAttemptResponse(attemptRepository.save(attempt));
    }

    @Override
    @Transactional(readOnly = true)
    public AttemptResponse getAttempt(String attemptId, String userId) {
        return mapper.toAttemptResponse(getAttemptOwned(attemptId, userId));
    }

    @Override
    @Transactional
    public List<GradingResultResponse> getResults(String attemptId, String userId) {
        getAttemptOwned(attemptId, userId); // access check

        List<AiPracticeAnswer> answers = answerRepository.findByAttemptIdAndDeletedFalse(attemptId);
        List<GradingResultResponse> responses = new ArrayList<>();

        for (AiPracticeAnswer answer : answers) {
            AiGradingJob job = jobRepository.findByAnswerIdAndDeletedFalse(answer.getId()).orElse(null);
            if (job == null) {
                responses.add(GradingResultResponse.builder()
                        .answerId(answer.getId())
                        .itemId(answer.getItemId())
                        .jobStatus(GradingJobStatus.PENDING)
                        .build());
                continue;
            }

            AiPracticeItem item = itemRepository.findByIdAndDeletedFalse(answer.getItemId()).orElse(null);
            if (shouldRecoverLegacyWritingFailure(job, item)) {
                recoverLegacyWritingFailure(job, answer.getAttemptId());
                job = jobRepository.findByIdAndDeletedFalse(job.getId()).orElse(job);
            }

            AiGradingResult result = (job.getStatus() == GradingJobStatus.COMPLETED)
                    ? resultRepository.findByGradingJobIdAndDeletedFalse(job.getId()).orElse(null)
                    : null;

            GradingResultResponse.GradingResultResponseBuilder rb = GradingResultResponse.builder()
                    .jobId(job.getId())
                    .providerJobId(job.getProviderJobId())
                    .errorMessage(job.getErrorMessage())
                    .answerId(answer.getId())
                    .itemId(answer.getItemId())
                    .jobStatus(job.getStatus())
                    .completedAt(job.getCompletedAt());

            if (result != null) {
                rb.normalizedScore(result.getNormalizedScore())
                        .normalizedMaxScore(result.getNormalizedMaxScore())
                        .normalizedLevel(result.getNormalizedLevel())
                        .deductionsJson(result.getDeductionsJson())
                        .feedbackText(result.getFeedbackText())
                        .transcriptText(result.getTranscriptText())
                        .analyticsJson(result.getAnalyticsJson());
            }

            responses.add(rb.build());
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public GradingJobResponse getJob(String jobId) {
        AiGradingJob job = jobRepository.findByIdAndDeletedFalse(jobId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "GradingJob not found: " + jobId));
        return mapper.toJobResponse(job);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private AiPracticeAttempt getAttemptOwned(String attemptId, String userId) {
        AiPracticeAttempt attempt = attemptRepository.findByIdAndDeletedFalse(attemptId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Attempt not found: " + attemptId));
        if (!attempt.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E227, "Access denied for attempt: " + attemptId);
        }
        return attempt;
    }

    private boolean shouldRecoverLegacyWritingFailure(AiGradingJob job, AiPracticeItem item) {
        if (job.getStatus() != GradingJobStatus.FAILED || job.getProviderJobId() != null || item == null) {
            return false;
        }

        if (item.getQuestionSubtype() == AiItemSubtype.AUDIO_COMPARE
                || item.getQuestionSubtype().name().startsWith("SPEAKING_")) {
            return false;
        }

        String errorMessage = job.getErrorMessage();
        return errorMessage != null
                && errorMessage.contains("HSK_API call failed (404)")
                && errorMessage.contains("/api/v2/grade");
    }

    private void recoverLegacyWritingFailure(AiGradingJob job, String attemptId) {
        if (job.getRequestPayloadJson() == null || job.getRequestPayloadJson().isBlank()) {
            return;
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(
                    job.getRequestPayloadJson(), new TypeReference<Map<String, Object>>() {});
            String rawResponse = hskApiClient.submitWritingSyncV1(payload);

            job.setResponsePayloadJson(rawResponse);
            job.setStatus(GradingJobStatus.COMPLETED);
            job.setCompletedAt(Instant.now());
            job.setErrorMessage(null);
            job.setProviderJobId(null);
            jobRepository.save(job);

            AiGradingResult result = normalizer.normalizeWriting(job.getId(), rawResponse);
            resultRepository.save(result);

            eventPublisher.publishEvent(
                    AiPracticeProgressUpdatedEvent.builder()
                            .attemptId(attemptId)
                            .occurredAt(Instant.now())
                            .build());

            log.info("Recovered legacy writing job {} from FAILED to COMPLETED", job.getId());
        } catch (Exception ex) {
            log.warn("Failed to recover legacy writing job {}: {}", job.getId(), ex.getMessage());
        }
    }
}
