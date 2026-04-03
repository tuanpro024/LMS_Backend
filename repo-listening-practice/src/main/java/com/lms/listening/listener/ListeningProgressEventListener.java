package com.lms.listening.listener;

import com.github.f4b6a3.ulid.UlidCreator;
import com.lms.listening.entity.ListeningStudySetProgress;
import com.lms.listening.event.ListeningStudySetProgressKafkaPayload;
import com.lms.listening.event.ListeningStudySetProgressUpdatedEvent;
import com.lms.listening.entity.enums.ProgressStatus;
import com.lms.listening.publisher.ListeningProgressEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListeningProgressEventListener {

    private final ListeningProgressEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProgressUpdated(ListeningStudySetProgressUpdatedEvent event) {
        ListeningStudySetProgress progress = event.getProgress();
        log.info("Handling status after commit for study set {}: status={}", progress.getStudySetId(), progress.getStatus());

        ListeningStudySetProgressKafkaPayload payload = ListeningStudySetProgressKafkaPayload.builder()
                .eventId(UlidCreator.getUlid().toString())
                .userId(progress.getUserId())
                .studySetId(progress.getStudySetId())
                .completedVideos(progress.getCompletedVideos())
                .totalVideos(progress.getTotalVideos())
                .progressPercentage(progress.getProgressPercentage())
                .completed(progress.getStatus() == ProgressStatus.COMPLETED)
                .completedAt(progress.getCompletedAt())
                .occurredAt(Instant.now())
                .build();

        publisher.publish(payload);
    }
}
