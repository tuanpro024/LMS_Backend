package com.lms.learningpath.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.learningpath.integration.event.FlashcardProgressEvent;
import com.lms.learningpath.service.impl.ProgressTrackingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashcardProgressKafkaListener {

    private final ObjectMapper objectMapper;
    private final ProgressTrackingServiceImpl progressTrackingService;

    @KafkaListener(topics = "${flashcard.progress.kafka.topic:flashcard.progress.events}", groupId = "${flashcard.progress.kafka.group:repo-learning-path-flashcard-progress}")
    public void onMessage(String payload) {
        try {
            FlashcardProgressEvent event = objectMapper.readValue(payload, FlashcardProgressEvent.class);
            progressTrackingService.syncFlashcardProgressEvent(event);
        } catch (Exception ex) {
            log.warn("Failed to consume flashcard progress event: {}", ex.getMessage());
        }
    }
}
