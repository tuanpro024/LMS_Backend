package com.lms.kanjiorigin.event;

import com.lms.kanjiorigin.service.KanjiProgressEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class KanjiProgressEventListener {

    private final KanjiProgressEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onKanjiProgressUpdated(KanjiStudySetProgressUpdatedEvent event) {
        eventPublisher.publish(event);
    }
}
