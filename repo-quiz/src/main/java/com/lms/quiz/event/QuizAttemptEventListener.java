package com.lms.quiz.event;

import com.lms.quiz.service.QuizProgressEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class QuizAttemptEventListener {

    private final QuizProgressEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuizAttemptSubmitted(QuizAttemptSubmittedEvent event) {
        eventPublisher.publish(event);
    }
}
