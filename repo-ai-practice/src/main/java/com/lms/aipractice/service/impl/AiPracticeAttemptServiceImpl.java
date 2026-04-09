package com.lms.aipractice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.LinkedHashMap;
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

        if (!answerRepository.existsByAttemptIdAndDeletedFalse(attemptId)) {
            throw new ApiException(ErrorCode.E227,
                    "Cannot submit attempt without answers: " + attemptId);
        }

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(Instant.now());
        AiPracticeAttempt saved = attemptRepository.save(attempt);

        // Handle race where all grading jobs completed before user clicked submit.
        // Re-aggregate immediately so status can advance to GRADED without waiting for another event.
        saved = refreshAttemptAggregate(saved);
        return mapper.toAttemptResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttemptResponse getAttempt(String attemptId, String userId) {
        return mapper.toAttemptResponse(getAttemptOwned(attemptId, userId));
    }

    @Override
    @Transactional
    public List<AttemptResponse> getLatestAttemptsByStudySetIds(List<String> studySetIds, String userId) {
        if (studySetIds == null || studySetIds.isEmpty()) {
            return List.of();
        }

        List<String> distinctStudySetIds = studySetIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();

        if (distinctStudySetIds.isEmpty()) {
            return List.of();
        }

        List<AiPracticeAttempt> attempts = attemptRepository
                .findByUserIdAndStudySetIdInAndDeletedFalseOrderByCreatedAtDesc(userId, distinctStudySetIds);

        Map<String, AiPracticeAttempt> latestSubmittedOrGradedByStudySet = new LinkedHashMap<>();
        Map<String, AiPracticeAttempt> latestAnyByStudySet = new LinkedHashMap<>();

        for (AiPracticeAttempt attempt : attempts) {
            String studySetId = attempt.getStudySetId();
            latestAnyByStudySet.putIfAbsent(studySetId, attempt);

            if (isLatestAttemptCandidate(attempt)) {
                latestSubmittedOrGradedByStudySet.putIfAbsent(studySetId, attempt);
            }
        }

        return distinctStudySetIds.stream()
                .map(studySetId -> latestSubmittedOrGradedByStudySet.getOrDefault(
                        studySetId,
                        latestAnyByStudySet.get(studySetId)))
                .filter(attempt -> attempt != null)
                .map(this::refreshAttemptAggregate)
                .map(mapper::toAttemptResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GradingResultResponse> getResults(String attemptId, String userId) {
        getAttemptOwned(attemptId, userId); // access check

        List<AiPracticeAnswer> answers = answerRepository.findByAttemptIdAndDeletedFalse(attemptId);
        List<GradingResultResponse> responses = new ArrayList<>();

        for (AiPracticeAnswer answer : answers) {
            AiGradingJob job = jobRepository
                    .findTopByAnswerIdAndDeletedFalseOrderByCreatedAtDesc(answer.getId())
                    .orElse(null);
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

            if (job.getStatus() == GradingJobStatus.COMPLETED && item != null) {
                result = healNormalizedResultIfNeeded(job, item, result);
            }

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

    private AiPracticeAttempt refreshAttemptAggregate(AiPracticeAttempt attempt) {
        List<AiPracticeAnswer> answers = answerRepository.findByAttemptIdAndDeletedFalse(attempt.getId());

        double totalScore = 0D;
        double maxScore = 0D;
        boolean allTerminal = !answers.isEmpty();

        for (AiPracticeAnswer answer : answers) {
            AiGradingJob latestJob = jobRepository
                    .findTopByAnswerIdAndDeletedFalseOrderByCreatedAtDesc(answer.getId())
                    .orElse(null);

            if (latestJob == null || !isTerminalJobStatus(latestJob.getStatus())) {
                allTerminal = false;
                continue;
            }

            AiGradingJob scoringJob = latestJob.getStatus() == GradingJobStatus.COMPLETED
                    ? latestJob
                    : jobRepository.findTopByAnswerIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                            answer.getId(),
                            GradingJobStatus.COMPLETED).orElse(null);

            if (scoringJob == null) {
                continue;
            }

            AiGradingResult result = resultRepository.findByGradingJobIdAndDeletedFalse(scoringJob.getId()).orElse(null);
            if (result == null) {
                continue;
            }

            if (result.getNormalizedScore() != null) {
                totalScore += result.getNormalizedScore();
            }
            if (result.getNormalizedMaxScore() != null) {
                maxScore += result.getNormalizedMaxScore();
            }
        }

        double progressPercent = maxScore > 0 ? (totalScore / maxScore) * 100D : 0D;
        attempt.setTotalScore(totalScore);
        attempt.setMaxScore(maxScore > 0 ? maxScore : null);
        attempt.setProgressPercent(progressPercent);

        if (allTerminal && shouldPromoteToGraded(attempt)) {
            attempt.setStatus(AttemptStatus.GRADED);
        }

        return attemptRepository.save(attempt);
    }

    private boolean isTerminalJobStatus(GradingJobStatus status) {
        return status == GradingJobStatus.COMPLETED
                || status == GradingJobStatus.FAILED
                || status == GradingJobStatus.TIMEOUT;
    }

    private boolean isLatestAttemptCandidate(AiPracticeAttempt attempt) {
        AttemptStatus status = attempt.getStatus();
        if (status == AttemptStatus.SUBMITTED || status == AttemptStatus.GRADED) {
            return true;
        }

        if (attempt.getSubmittedAt() != null) {
            return true;
        }

        return attempt.getTotalScore() != null
                || attempt.getMaxScore() != null
                || attempt.getProgressPercent() != null;
    }

    private boolean shouldPromoteToGraded(AiPracticeAttempt attempt) {
        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            return true;
        }

        return attempt.getStatus() == AttemptStatus.IN_PROGRESS
                && attempt.getSubmittedAt() != null;
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
                    job.getRequestPayloadJson(), new TypeReference<Map<String, Object>>() {
                    });
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

    private AiGradingResult healNormalizedResultIfNeeded(AiGradingJob job, AiPracticeItem item,
            AiGradingResult existing) {
        String rawJson = job.getResponsePayloadJson();
        if (rawJson == null || rawJson.isBlank()) {
            return existing;
        }

        boolean speakingSubtype = item.getQuestionSubtype() != null
                && item.getQuestionSubtype().name().startsWith("SPEAKING_");
        boolean audioSubtype = item.getQuestionSubtype() == AiItemSubtype.AUDIO_COMPARE;

        if (!needsNormalizationRefresh(job, existing, speakingSubtype, audioSubtype)) {
            return existing;
        }

        AiGradingResult normalized = speakingSubtype
                ? normalizer.normalizeSpeaking(job.getId(), rawJson)
                : audioSubtype
                        ? normalizer.normalizeAudioCompare(job.getId(), rawJson)
                        : normalizer.normalizeWriting(job.getId(), rawJson);

        if (existing != null) {
            existing.setNormalizedScore(normalized.getNormalizedScore());
            existing.setNormalizedMaxScore(normalized.getNormalizedMaxScore());
            existing.setNormalizedLevel(normalized.getNormalizedLevel());
            existing.setDeductionsJson(normalized.getDeductionsJson());
            existing.setFeedbackText(normalized.getFeedbackText());
            existing.setTranscriptText(normalized.getTranscriptText());
            existing.setAnalyticsJson(normalized.getAnalyticsJson());
            return resultRepository.save(existing);
        }

        return resultRepository.save(normalized);
    }

    private boolean needsNormalizationRefresh(
            AiGradingJob job,
            AiGradingResult existing,
            boolean speakingSubtype,
            boolean audioSubtype) {

        if (existing == null) {
            return true;
        }

        if (speakingSubtype || audioSubtype) {
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(job.getResponsePayloadJson());
            JsonNode data = root.has("result") ? root.path("result") : root;

            if (data.isMissingNode()) {
                return false;
            }

            if (!root.has("result")) {
                return false;
            }

            Double providerMaxScore = null;
            if (data.has("max_score_per_question")) {
                providerMaxScore = data.path("max_score_per_question").asDouble();
            } else if (data.has("max_score")) {
                providerMaxScore = data.path("max_score").asDouble();
            }

            if (providerMaxScore == null) {
                return false;
            }

            Double storedMaxScore = existing.getNormalizedMaxScore();
            if (storedMaxScore == null) {
                return true;
            }

            return Math.abs(storedMaxScore - providerMaxScore) > 0.0001
                    || existing.getAnalyticsJson() == null;
        } catch (Exception ex) {
            log.debug("Skip normalization refresh check for job {} due to parse error: {}", job.getId(),
                    ex.getMessage());
            return false;
        }
    }
}
