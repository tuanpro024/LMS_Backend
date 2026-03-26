package com.lms.flashcard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.flashcard.event.FlashcardProgressUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashcardProgressEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${flashcard.progress.kafka.topic:flashcard.progress.events}")
    private String topic;

    public void publish(FlashcardProgressUpdatedEvent event) {
        if (event == null || event.userId() == null || event.userId().isBlank()) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.userId(), payload);
            log.info("Published flashcard progress event {} for user {}", event.eventId(), event.userId());
        } catch (Exception ex) {
            log.warn("Failed to publish flashcard progress event {}: {}", event.eventId(), ex.getMessage());
        }
    }
}
