package com.lms.videocourse.service;

import com.lms.videocourse.dto.event.QuizProgressEvent;
import com.lms.videocourse.entity.ConsumedQuizProgressEvent;
import com.lms.videocourse.entity.VideoModule;
import com.lms.videocourse.entity.VideoPracticeModuleProgress;
import com.lms.videocourse.entity.VideoStep;
import com.lms.videocourse.entity.enums.ModuleType;
import com.lms.videocourse.entity.enums.ProgressStatus;
import com.lms.videocourse.repository.ConsumedQuizProgressEventRepository;
import com.lms.videocourse.repository.VideoModuleRepository;
import com.lms.videocourse.repository.VideoPracticeModuleProgressRepository;
import com.lms.videocourse.repository.VideoStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizModuleProgressSyncService {

    private final VideoModuleRepository videoModuleRepository;
    private final VideoPracticeModuleProgressRepository practiceProgressRepository;
    private final ConsumedQuizProgressEventRepository consumedEventRepository;
    private final VideoStepRepository videoStepRepository;
    private final IVideoProgressService videoProgressService;

    @Transactional
    public void syncQuizProgress(QuizProgressEvent event) {
        log.info("Processing quiz progress event {} for user {} studySet {}",
                event.getEventId(), event.getUserId(), event.getStudySetId());

        // 1. Idempotency check
        if (consumedEventRepository.existsById(event.getEventId())) {
            log.info("Event {} already consumed, skipping", event.getEventId());
            return;
        }

        // 2. We only care about completed study sets for video course progression
        if (!Boolean.TRUE.equals(event.getCompleted())) {
            log.debug("Event {} is not a completion event, marking consumed and skipping logic", event.getEventId());
            markEventConsumed(event);
            return;
        }

        // 3. Find all active QUIZ modules using this studySetId
        List<VideoModule> linkedModules = videoModuleRepository
                .findByContentSetIdAndModuleTypeAndIsActiveTrue(event.getStudySetId(), ModuleType.QUIZ);

        if (linkedModules.isEmpty()) {
            log.debug("No active QUIZ modules found linked to studySet {}", event.getStudySetId());
            markEventConsumed(event);
            return;
        }

        // 4. Update or create progress for each linked module
        Instant completeTime = event.getCompletedAt() != null ? event.getCompletedAt() : 
                               (event.getOccurredAt() != null ? event.getOccurredAt() : Instant.now());
                               
        for (VideoModule module : linkedModules) {
            VideoPracticeModuleProgress progress = practiceProgressRepository
                    .findByUserIdAndVideoModuleId(event.getUserId(), module.getId())
                    .orElseGet(() -> VideoPracticeModuleProgress.builder()
                            .userId(event.getUserId())
                            .videoModuleId(module.getId())
                            .stepId(module.getStepId())
                            .studySetId(getCourseStudySetId(module.getStepId()))
                            .contentSetId(event.getStudySetId())
                            .moduleType(ModuleType.QUIZ)
                            .status(ProgressStatus.NOT_STARTED)
                            .progressPercentage(0.0)
                            .build());

            progress.setStatus(ProgressStatus.COMPLETED);
            progress.setProgressPercentage(100.0);
            
            if (progress.getFirstStartedAt() == null) {
                progress.setFirstStartedAt(completeTime);
            }
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(completeTime);
            }
            
            progress.setLastSyncedEventId(event.getEventId());
            progress.setLastSyncedAt(Instant.now());

            practiceProgressRepository.save(progress);
            log.info("Marked QUIZ module {} COMPLETE for user {}", module.getId(), event.getUserId());

            // 5. Trigger Rollup
            try {
                videoProgressService.getStepProgress(event.getUserId(), module.getStepId());
                
                VideoStep step = videoStepRepository.findById(module.getStepId()).orElse(null);
                if (step != null) {
                    videoProgressService.getCourseProgress(event.getUserId(), step.getStudySetId());
                }
            } catch (Exception ex) {
                log.error("Failed to calculate rollup progress for module {}", module.getId(), ex);
            }
        }

        // 6. Save idempotency record
        markEventConsumed(event);
    }

    private void markEventConsumed(QuizProgressEvent event) {
        ConsumedQuizProgressEvent consumedEvent = ConsumedQuizProgressEvent.builder()
                .eventId(event.getEventId())
                .userId(event.getUserId())
                .studySetId(event.getStudySetId())
                .consumedAt(Instant.now())
                .build();
        consumedEventRepository.save(consumedEvent);
        log.debug("Marked event {} as consumed", event.getEventId());
    }

    private String getCourseStudySetId(String stepId) {
        return videoStepRepository.findById(stepId)
                .map(VideoStep::getStudySetId)
                .orElse(null);
    }
}
