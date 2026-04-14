package com.lms.learningpath.event;

import com.lms.content.common.event.StudySetDeletedEvent;
import com.lms.learningpath.entity.LearningPath;
import com.lms.learningpath.repository.LearningPathProgressRepository;
import com.lms.learningpath.repository.LearningPathRepository;
import com.lms.learningpath.repository.StepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudySetDeletionListener {

    private final LearningPathRepository learningPathRepository;
    private final StepRepository stepRepository;
    private final LearningPathProgressRepository progressRepository;

    /**
     * Listen for StudySet deletion events and clean up associated Learning Paths and Steps.
     */
    @EventListener
    @Transactional
    public void handleStudySetDeletedEvent(StudySetDeletedEvent event) {
        String studySetId = event.getStudySetId();
        log.info("Cleaning up Learning Path data for deleted StudySet: {}", studySetId);
        
        try {
            List<LearningPath> learningPaths = learningPathRepository.findByStudySetId(studySetId);
            for (LearningPath lp : learningPaths) {
                String lpId = lp.getId();
                log.info("Deleting Learning Path {} and its steps/progress", lpId);
                
                // Delete steps
                stepRepository.deleteByLearningPathId(lpId);
                
                // Delete progress data
                progressRepository.deleteByLearningPathId(lpId);
                
                // Delete the path itself
                learningPathRepository.delete(lp);
            }
            log.info("Successfully cleaned up {} Learning Paths for StudySet: {}", learningPaths.size(), studySetId);
        } catch (Exception e) {
            log.error("Failed to clean up Learning Path data for StudySet {}: {}", studySetId, e.getMessage());
        }
    }
}
