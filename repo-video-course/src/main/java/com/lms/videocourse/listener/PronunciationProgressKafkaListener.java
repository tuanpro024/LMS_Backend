package com.lms.videocourse.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.videocourse.dto.event.PronunciationProgressEvent;
import com.lms.videocourse.service.PronunciationModuleProgressSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PronunciationProgressKafkaListener {

    private final ObjectMapper objectMapper;
    private final PronunciationModuleProgressSyncService syncService;

    @KafkaListener(topics = "${app.kafka.topics.pronunciation-progress-events:pronunciation.progress.events}", groupId = "video-course-pronunciation-progress-group")
    public void consumePronunciationProgress(String message) {
        try {
            log.info("Received pronunciation progress event: {}", message);
            PronunciationProgressEvent event = objectMapper.readValue(message, PronunciationProgressEvent.class);
            syncService.syncProgress(event);
        } catch (Exception e) {
            log.error("Error processing pronunciation progress event", e);
        }
    }
}
