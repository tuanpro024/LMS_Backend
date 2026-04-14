package com.lms.aipractice.event;

import com.lms.aipractice.repository.AiPracticeItemRepository;
import com.lms.content.common.event.StudySetDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudySetDeletionListener {

    private final AiPracticeItemRepository aiPracticeItemRepository;

    /**
     * Listen for StudySet deletion events and clean up associated AI practice items.
     */
    @EventListener
    @Transactional
    public void handleStudySetDeletedEvent(StudySetDeletedEvent event) {
        String studySetId = event.getStudySetId();
        log.info("Cleaning up AI practice items for deleted StudySet: {}", studySetId);
        
        try {
            // Hard delete items associated with the study set
            aiPracticeItemRepository.deleteByStudySetId(studySetId);
            log.info("Successfully deleted AI practice items for StudySet: {}", studySetId);
        } catch (Exception e) {
            log.error("Failed to delete AI practice items for StudySet {}: {}", studySetId, e.getMessage());
        }
    }
}
