package com.lms.videocourse.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.videocourse.dto.event.ListeningProgressEvent;
import com.lms.videocourse.service.ListeningModuleProgressSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListeningProgressKafkaListener {

    private final ListeningModuleProgressSyncService syncService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.listening-progress-events}", groupId = "video-course-listening-progress-group")
    public void listen(String message) {
        log.info("Received listening progress event: {}", message);
        try {
            ListeningProgressEvent event = objectMapper.readValue(message, ListeningProgressEvent.class);
            syncService.syncProgress(event);
        } catch (JsonProcessingException e) {
            log.error("Error parsing listening progress event: {}", message, e);
        }
    }
}
