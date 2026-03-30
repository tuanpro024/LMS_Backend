package com.lms.quiz.event;

import com.lms.quiz.service.QuizStudySetProgressEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class QuizStudySetProgressEventListener {

    private final QuizStudySetProgressEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuizStudySetProgressUpdated(QuizStudySetProgressUpdatedEvent event) {
        eventPublisher.publish(event);
    }
}
