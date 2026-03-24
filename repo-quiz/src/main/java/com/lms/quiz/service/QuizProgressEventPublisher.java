package com.lms.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.quiz.event.QuizAttemptSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizProgressEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${quiz.progress.kafka.topic:quiz.progress.events}")
    private String topic;

    public void publish(QuizAttemptSubmittedEvent event) {
        if (event == null || event.userId() == null || event.userId().isBlank()) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.userId(), payload);
            log.info("Published quiz progress event {} for user {}", event.eventId(), event.userId());
        } catch (Exception ex) {
            log.warn("Failed to publish quiz progress event {}: {}", event.eventId(), ex.getMessage());
        }
    }
}
