package com.lms.videocourse.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.videocourse.dto.event.WritingProgressEvent;
import com.lms.videocourse.service.WritingModuleProgressSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WritingProgressKafkaListener {

    private final ObjectMapper objectMapper;
    private final WritingModuleProgressSyncService syncService;

    @KafkaListener(topics = "${writing.progress.kafka.topic:writing.progress.events}", groupId = "${spring.kafka.consumer.group-id:repo-video-course}")
    public void listenWritingProgressEvent(@Payload String message) {
        log.debug("Received raw message from writing.progress.events: {}", message);
        try {
            WritingProgressEvent event = objectMapper.readValue(message, WritingProgressEvent.class);
            if (event.getEventId() == null || event.getUserId() == null) {
                log.warn("Invalid writing progress event received: {}", message);
                return;
            }
            syncService.syncWritingProgress(event);
        } catch (Exception e) {
            log.error("Failed to process writing progress event: {}", message, e);
            // Non-blocking log
        }
    }
}
