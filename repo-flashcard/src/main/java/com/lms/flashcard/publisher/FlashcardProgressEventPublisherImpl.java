package com.lms.flashcard.publisher;

import com.lms.flashcard.event.FlashcardStudySetProgressKafkaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashcardProgressEventPublisherImpl implements FlashcardProgressEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.flashcard-progress-events:flashcard.progress.events}")
    private String progressTopic;

    @Override
    public void publishProgressUpdatedEvent(FlashcardStudySetProgressKafkaPayload payload) {
        log.info("Publishing flashcard progress event for user {} and study set {} to topic {}", 
                payload.getUserId(), payload.getStudySetId(), progressTopic);
        // Producer logic: Kafka Key = userId to ensure order per user
        kafkaTemplate.send(progressTopic, payload.getUserId(), payload);
    }
}
