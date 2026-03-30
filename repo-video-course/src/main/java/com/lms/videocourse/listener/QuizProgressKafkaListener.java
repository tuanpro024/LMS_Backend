package com.lms.videocourse.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.videocourse.dto.event.QuizProgressEvent;
import com.lms.videocourse.service.QuizModuleProgressSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizProgressKafkaListener {

    private final ObjectMapper objectMapper;
    private final QuizModuleProgressSyncService syncService;

    @KafkaListener(topics = "${quiz.study-set.progress.kafka.topic:quiz.study-set.progress.events}", groupId = "${spring.kafka.consumer.group-id:repo-video-course}")
    public void listenQuizProgressEvent(@Payload String message) {
        log.debug("Received raw message from quiz.study-set.progress.events: {}", message);
        try {
            QuizProgressEvent event = objectMapper.readValue(message, QuizProgressEvent.class);
            if (event.getEventId() == null || event.getUserId() == null) {
                log.warn("Invalid quiz progress event received: {}", message);
                return;
            }
            syncService.syncQuizProgress(event);
        } catch (Exception e) {
            log.error("Failed to process quiz progress event: {}", message, e);
        }
    }
}
