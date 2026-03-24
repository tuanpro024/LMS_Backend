package com.lms.learningpath.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.learningpath.integration.event.QuizProgressEvent;
import com.lms.learningpath.service.impl.ProgressTrackingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizProgressKafkaListener {

    private final ObjectMapper objectMapper;
    private final ProgressTrackingServiceImpl progressTrackingService;

    @KafkaListener(
            topics = "${quiz.progress.kafka.topic:quiz.progress.events}",
            groupId = "${quiz.progress.kafka.group:repo-learning-path-quiz-progress}")
    public void onMessage(String payload) {
        try {
            QuizProgressEvent event = objectMapper.readValue(payload, QuizProgressEvent.class);
            progressTrackingService.syncQuizProgressEvent(event);
        } catch (Exception ex) {
            log.warn("Failed to consume quiz progress event: {}", ex.getMessage());
        }
    }
}
