package com.lms.writing.event;

import com.lms.writing.service.WritingProgressEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WritingProgressEventListener {

    private final WritingProgressEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWritingProgressUpdated(WritingProgressUpdatedEvent event) {
        eventPublisher.publish(event);
    }
}