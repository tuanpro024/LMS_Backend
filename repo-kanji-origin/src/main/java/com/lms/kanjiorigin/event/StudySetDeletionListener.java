package com.lms.kanjiorigin.event;

import com.lms.content.common.event.StudySetDeletedEvent;
import com.lms.kanjiorigin.repository.KanjiOriginRepository;
import com.lms.kanjiorigin.repository.KanjiStudySetProgressRepository;
import com.lms.kanjiorigin.repository.UserKanjiProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudySetDeletionListener {

    private final KanjiOriginRepository kanjiOriginRepository;
    private final UserKanjiProgressRepository userKanjiProgressRepository;
    private final KanjiStudySetProgressRepository kanjiStudySetProgressRepository;

    /**
     * Listen for StudySet deletion events and clean up associated Kanji Origins.
     */
    @EventListener
    @Transactional
    public void handleStudySetDeletedEvent(StudySetDeletedEvent event) {
        String studySetId = event.getStudySetId();
        log.info("Cleaning up Kanji Origin data for deleted StudySet: {}", studySetId);
        
        try {
            userKanjiProgressRepository.deleteByStudySetId(studySetId);
            kanjiStudySetProgressRepository.deleteByStudySetId(studySetId);
            kanjiOriginRepository.deleteByStudySetId(studySetId);
            log.info("Successfully cleaned up all Kanji Origins for StudySet: {}", studySetId);
        } catch (Exception e) {
            log.error("Failed to clean up Kanji Origin data for StudySet {}: {}", studySetId, e.getMessage());
        }
    }
}
