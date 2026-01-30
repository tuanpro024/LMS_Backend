package com.lms.learningpath.service.impl;

import com.lms.learningpath.entity.LearningEvent;
import com.lms.learningpath.entity.enums.EventType;
import com.lms.learningpath.repository.LearningEventRepository;
import com.lms.learningpath.service.LearningEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningEventServiceImpl implements LearningEventService {

    private final LearningEventRepository eventRepository;

    @Override
    @Transactional
    public void logEvent(
            String userId,
            EventType eventType,
            String studySetId,
            String moduleId,
            Integer score,
            Integer durationSeconds,
            String metadata
    ) {
        LearningEvent event = LearningEvent.builder()
                .userId(userId)
                .eventType(eventType)
                .studySetId(studySetId)
                .moduleId(moduleId)
                .score(score)
                .durationSeconds(durationSeconds)
                .metadata(metadata)
                .occurredAt(Instant.now())
                .build();

        eventRepository.save(event);

        log.debug("Logged event: {} for user: {} on module: {}", eventType, userId, moduleId);
    }

    @Override
    @Transactional
    public void logSimpleEvent(
            String userId,
            EventType eventType,
            String studySetId,
            String moduleId
    ) {
        logEvent(userId, eventType, studySetId, moduleId, null, null, null);
    }
}