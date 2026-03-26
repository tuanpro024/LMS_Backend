package com.lms.learningpath.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.learningpath.integration.event.WritingProgressEvent;
import com.lms.learningpath.service.impl.ProgressTrackingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WritingProgressKafkaListener {

    private final ObjectMapper objectMapper;
    private final ProgressTrackingServiceImpl progressTrackingService;

    @KafkaListener(topics = "${writing.progress.kafka.topic:writing.progress.events}", groupId = "${writing.progress.kafka.group:repo-learning-path-writing-progress}")
    public void onMessage(String payload) {
        try {
            WritingProgressEvent event = objectMapper.readValue(payload, WritingProgressEvent.class);
            progressTrackingService.syncWritingProgressEvent(event);
        } catch (Exception ex) {
            log.warn("Failed to consume writing progress event: {}", ex.getMessage());
        }
    }
}