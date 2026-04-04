package com.lms.pronunciation.publisher;

import com.lms.pronunciation.event.PronunciationStudySetProgressKafkaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PronunciationProgressEventPublisherImpl implements PronunciationProgressEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.pronunciation-progress-events:pronunciation.progress.events}")
    private String progressTopic;

    @Override
    public void publishProgressUpdatedEvent(PronunciationStudySetProgressKafkaPayload payload) {
        try {
            log.info("Publishing pronunciation progress event for user {} and study set {} to topic {}", 
                    payload.getUserId(), payload.getStudySetId(), progressTopic);
            // Producer logic: Kafka Key = userId to ensure order per user
            kafkaTemplate.send(progressTopic, payload.getUserId(), payload);
        } catch (Exception e) {
            log.error("Failed to publish pronunciation progress event", e);
        }
    }
}
