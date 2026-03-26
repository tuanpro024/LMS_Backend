package com.lms.videocourse.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.videocourse.dto.event.FlashcardProgressEvent;
import com.lms.videocourse.service.FlashcardModuleProgressSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashcardProgressKafkaListener {

    private final ObjectMapper objectMapper;
    private final FlashcardModuleProgressSyncService syncService;

    @KafkaListener(topics = "${app.kafka.topics.flashcard-progress-events:flashcard.progress.events}", groupId = "video-course-flashcard-progress-group")
    public void consumeFlashcardProgress(String message) {
        try {
            log.info("Received flashcard progress event: {}", message);
            FlashcardProgressEvent event = objectMapper.readValue(message, FlashcardProgressEvent.class);
            syncService.syncProgress(event);
        } catch (Exception e) {
            log.error("Error processing flashcard progress event", e);
        }
    }
}
