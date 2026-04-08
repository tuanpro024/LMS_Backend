package com.lms.aipractice.event;

import com.lms.aipractice.entity.AiGradingJob;
import com.lms.aipractice.entity.AiGradingResult;
import com.lms.aipractice.entity.AiPracticeAnswer;
import com.lms.aipractice.entity.AiPracticeAttempt;
import com.lms.aipractice.entity.enums.AttemptStatus;
import com.lms.aipractice.entity.enums.GradingJobStatus;
import com.lms.aipractice.publisher.AiPracticeProgressEventPublisher;
import com.lms.aipractice.repository.AiGradingJobRepository;
import com.lms.aipractice.repository.AiGradingResultRepository;
import com.lms.aipractice.repository.AiPracticeAnswerRepository;
import com.lms.aipractice.repository.AiPracticeAttemptRepository;
import com.lms.aipractice.repository.AiPracticeItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Listens to AiPracticeProgressUpdatedEvent published internally,
 * aggregates scores from all graded jobs in the attempt,
 * updates attempt progress, and publishes a Kafka event.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiPracticeProgressEventListener {

    private final AiPracticeAttemptRepository attemptRepository;
    private final AiPracticeAnswerRepository answerRepository;
    private final AiPracticeItemRepository itemRepository;
    private final AiGradingJobRepository jobRepository;
    private final AiGradingResultRepository resultRepository;
    private final AiPracticeProgressEventPublisher kafkaPublisher;

    @EventListener
    @Async
    @Transactional
    public void onProgressUpdated(AiPracticeProgressUpdatedEvent event) {
        log.debug("Handling progress event for attempt={}", event.getAttemptId());

        AiPracticeAttempt attempt = attemptRepository.findByIdAndDeletedFalse(event.getAttemptId())
                .orElse(null);
        if (attempt == null) return;

        // Aggregate scores from all COMPLETED grading results for this attempt
        List<AiPracticeAnswer> answers = answerRepository.findByAttemptIdAndDeletedFalse(event.getAttemptId());
        List<AiGradingJob> jobs = jobRepository.findByAttemptIdAndDeletedFalse(event.getAttemptId());

        int totalAnswers = answers.size();
        int totalItems = (int) itemRepository.findByStudySetIdAndDeletedFalseOrderByContentIndexAsc(
                attempt.getStudySetId()).stream().filter(i -> !i.isDeleted()).count();

        double totalScore = 0;
        double maxScore = 0;
        int gradedCount = 0;

        for (AiGradingJob job : jobs) {
            if (job.getStatus() == GradingJobStatus.COMPLETED) {
                AiGradingResult result = resultRepository
                        .findByGradingJobIdAndDeletedFalse(job.getId()).orElse(null);
                if (result != null) {
                    if (result.getNormalizedScore() != null) totalScore += result.getNormalizedScore();
                    if (result.getNormalizedMaxScore() != null) maxScore += result.getNormalizedMaxScore();
                    gradedCount++;
                }
            }
        }

        boolean canPromote = attempt.getStatus() == AttemptStatus.SUBMITTED
            || (attempt.getStatus() == AttemptStatus.IN_PROGRESS && attempt.getSubmittedAt() != null);

        boolean completed = gradedCount > 0 && gradedCount >= totalAnswers && canPromote;

        double progressPercent = maxScore > 0 ? (totalScore / maxScore) * 100 : 0;

        // Update attempt aggregate scores
        attempt.setTotalScore(totalScore);
        attempt.setMaxScore(maxScore > 0 ? maxScore : null);
        attempt.setProgressPercent(progressPercent);
        if (completed) {
            attempt.setStatus(AttemptStatus.GRADED);
        }
        attemptRepository.save(attempt);

        // Publish Kafka event
        kafkaPublisher.publishProgressUpdated(
                AiPracticeProgressKafkaPayload.builder()
                        .eventId(UUID.randomUUID().toString())
                        .userId(attempt.getUserId())
                        .studySetId(attempt.getStudySetId())
                        .attemptId(attempt.getId())
                        .gradedAnswers(gradedCount)
                        .totalAnswers(totalAnswers)
                        .totalScore(totalScore)
                        .maxScore(maxScore)
                        .progressPercent(progressPercent)
                        .completed(completed)
                        .occurredAt(Instant.now())
                        .build());
    }
}
