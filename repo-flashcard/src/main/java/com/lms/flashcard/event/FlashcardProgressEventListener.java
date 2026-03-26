package com.lms.flashcard.event;

import com.lms.flashcard.service.FlashcardProgressEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FlashcardProgressEventListener {

    private final FlashcardProgressEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFlashcardProgressUpdated(FlashcardProgressUpdatedEvent event) {
        eventPublisher.publish(event);
    }
}
