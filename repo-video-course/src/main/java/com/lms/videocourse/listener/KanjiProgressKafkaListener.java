package com.lms.videocourse.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.videocourse.dto.event.KanjiProgressEvent;
import com.lms.videocourse.service.KanjiModuleProgressSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KanjiProgressKafkaListener {

    private final ObjectMapper objectMapper;
    private final KanjiModuleProgressSyncService syncService;

    @KafkaListener(topics = "${kanji.progress.kafka.topic:kanji.progress.events}", groupId = "${spring.kafka.consumer.group-id:repo-video-course}")
    public void listenKanjiProgressEvent(@Payload String message) {
        log.debug("Received raw message from kanji.progress.events: {}", message);
        try {
            KanjiProgressEvent event = objectMapper.readValue(message, KanjiProgressEvent.class);
            if (event.getEventId() == null || event.getUserId() == null) {
                log.warn("Invalid kanji progress event received: {}", message);
                return;
            }
            syncService.syncKanjiProgress(event);
        } catch (Exception e) {
            log.error("Failed to process kanji progress event: {}", message, e);
        }
    }
}
