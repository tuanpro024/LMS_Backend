package com.lms.listening.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.listening.event.ListeningStudySetProgressKafkaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListeningProgressEventPublisherImpl implements ListeningProgressEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.listening-progress-events:listening.progress.events}")
    private String topic;

    @Override
    public void publish(ListeningStudySetProgressKafkaPayload event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            log.info("Publishing listening progress event to topic {}: {}", topic, message);
            kafkaTemplate.send(topic, event.getUserId(), message);
        } catch (JsonProcessingException e) {
            log.error("Error serializing listening progress event", e);
        }
    }
}
