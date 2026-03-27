package com.lms.learningpath.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.learningpath.integration.event.KanjiProgressEvent;
import com.lms.learningpath.service.impl.ProgressTrackingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KanjiProgressKafkaListener {

    private final ObjectMapper objectMapper;
    private final ProgressTrackingServiceImpl progressTrackingService;

    @KafkaListener(topics = "${kanji.progress.kafka.topic:kanji.progress.events}", groupId = "${kanji.progress.kafka.group:repo-learning-path-kanji-progress}")
    public void onMessage(String payload) {
        try {
            KanjiProgressEvent event = objectMapper.readValue(payload, KanjiProgressEvent.class);
            progressTrackingService.syncKanjiProgressEvent(event);
        } catch (Exception ex) {
            log.warn("Failed to consume kanji progress event: {}", ex.getMessage());
        }
    }
}
