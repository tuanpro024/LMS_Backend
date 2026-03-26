package com.lms.flashcard.listener;

import com.lms.flashcard.event.FlashcardStudySetProgressKafkaPayload;
import com.lms.flashcard.event.FlashcardStudySetProgressUpdatedEvent;
import com.lms.flashcard.publisher.FlashcardProgressEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashcardProgressEventListener {

    private final FlashcardProgressEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProgressUpdatedEvent(FlashcardStudySetProgressUpdatedEvent event) {
        try {
            log.info("Handling after-commit event for flashcard progress update, userId: {}, studySetId: {}", 
                    event.getUserId(), event.getStudySetId());
                    
            FlashcardStudySetProgressKafkaPayload payload = FlashcardStudySetProgressKafkaPayload.builder()
                    .eventId(UUID.randomUUID().toString())
                    .userId(event.getUserId())
                    .studySetId(event.getStudySetId())
                    .learnedCards(event.getLearnedCards())
                    .totalCards(event.getTotalCards())
                    .progressPercentage(event.getProgressPercentage())
                    .completed(event.isCompleted())
                    .completedAt(event.getCompletedAt())
                    .occurredAt(event.getOccurredAt())
                    .build();
                    
            publisher.publishProgressUpdatedEvent(payload);
        } catch (Exception e) {
            log.error("Failed to publish flashcard progress updated event for userId: {}, studySetId: {}", 
                    event.getUserId(), event.getStudySetId(), e);
        }
    }
}
