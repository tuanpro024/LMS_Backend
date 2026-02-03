package com.lms.multimedia.listener;

import com.lms.content.common.event.StudySetDeletedEvent;
import com.lms.multimedia.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class MultimediaEventListener {

    private final VideoService videoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStudySetDeleted(StudySetDeletedEvent event) {
        log.info("Handling StudySetDeletedEvent for id: {}", event.getStudySetId());
        try {
            videoService.orphanAndSoftDeleteVideosByStudySetId(event.getStudySetId());
        } catch (Exception e) {
            log.error("Error handling StudySetDeletedEvent for id: {}", event.getStudySetId(), e);
        }
    }
}
