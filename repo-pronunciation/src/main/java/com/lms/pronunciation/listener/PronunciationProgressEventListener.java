package com.lms.pronunciation.listener;

import com.lms.pronunciation.event.PronunciationStudySetProgressKafkaPayload;
import com.lms.pronunciation.event.PronunciationStudySetProgressUpdatedEvent;
import com.lms.pronunciation.publisher.PronunciationProgressEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PronunciationProgressEventListener {

    private final PronunciationProgressEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProgressUpdatedEvent(PronunciationStudySetProgressUpdatedEvent event) {
        try {
            log.info("Handling after-commit event for pronunciation progress update, userId: {}, studySetId: {}", 
                    event.getUserId(), event.getStudySetId());
                    
            PronunciationStudySetProgressKafkaPayload payload = PronunciationStudySetProgressKafkaPayload.builder()
                    .eventId(UUID.randomUUID().toString())
                    .userId(event.getUserId())
                    .studySetId(event.getStudySetId())
                    .learnedItems(event.getLearnedItems())
                    .totalItems(event.getTotalItems())
                    .progressPercentage(event.getProgressPercentage())
                    .completed(event.isCompleted())
                    .completedAt(event.getCompletedAt())
                    .occurredAt(event.getOccurredAt())
                    .build();
                    
            publisher.publishProgressUpdatedEvent(payload);
        } catch (Exception e) {
            log.error("Failed to publish pronunciation progress updated event for userId: {}, studySetId: {}", 
                    event.getUserId(), event.getStudySetId(), e);
        }
    }
}
