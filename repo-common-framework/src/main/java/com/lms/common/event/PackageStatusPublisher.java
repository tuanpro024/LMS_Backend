package com.lms.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publish {@link PackageStatusEvent} lên Kafka khi trạng thái package thay đổi.
 *
 * <p>Topic được cấu hình qua property {@code package.status.publisher.topic}
 * (default: {@code package.status.events}).</p>
 */
@Component
public class PackageStatusPublisher {

    private static final Logger log = LoggerFactory.getLogger(PackageStatusPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public PackageStatusPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${package.status.publisher.topic:package.status.events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    /**
     * Publish event thay đổi trạng thái package lên Kafka.
     *
     * @param event event chứa thông tin package và lý do thay đổi
     */
    public void publish(PackageStatusEvent event) {
        if (event == null || event.packageId() == null || event.packageId().isBlank()) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.packageId(), payload);
            log.info("Published PackageStatusEvent: packageId={}, status={}, reason={}",
                    event.packageId(), event.status(), event.reason());
        } catch (Exception ex) {
            log.warn("Failed to publish PackageStatusEvent for packageId={}: {}",
                    event.packageId(), ex.getMessage());
        }
    }
}
