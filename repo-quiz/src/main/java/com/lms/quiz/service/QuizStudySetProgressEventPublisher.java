package com.lms.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.quiz.event.QuizStudySetProgressUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizStudySetProgressEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${quiz.study-set.progress.kafka.topic:quiz.study-set.progress.events}")
    private String topic;

    public void publish(QuizStudySetProgressUpdatedEvent event) {
        if (event == null || event.userId() == null || event.userId().isBlank()) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.userId(), payload);
            log.info("Published quiz study-set progress event {} for user {}", event.eventId(), event.userId());
        } catch (Exception ex) {
            log.warn("Failed to publish quiz study-set progress event {}: {}", event.eventId(), ex.getMessage());
        }
    }
}
