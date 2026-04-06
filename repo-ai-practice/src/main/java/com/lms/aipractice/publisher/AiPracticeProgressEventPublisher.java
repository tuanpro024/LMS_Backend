package com.lms.aipractice.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.event.AiPracticeProgressKafkaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiPracticeProgressEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.ai-practice-progress-events:ai-practice.progress.events}")
    private String progressTopic;

    public void publishProgressUpdated(AiPracticeProgressKafkaPayload payload) {
        try {
            log.info("Publishing AI practice progress event: user={} studySet={} attempt={}",
                    payload.getUserId(), payload.getStudySetId(), payload.getAttemptId());
            String jsonPayload = objectMapper.writeValueAsString(payload);
            // Kafka key = userId để đảm bảo thứ tự per-user
            kafkaTemplate.send(progressTopic, payload.getUserId(), jsonPayload);
        } catch (Exception e) {
            log.error("Failed to publish AI practice progress event for attempt={}: {}",
                    payload.getAttemptId(), e.getMessage());
        }
    }
}
