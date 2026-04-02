package com.lms.videocourse.service;

import com.lms.videocourse.dto.event.KanjiProgressEvent;
import com.lms.videocourse.entity.ConsumedKanjiProgressEvent;
import com.lms.videocourse.entity.VideoModule;
import com.lms.videocourse.entity.VideoPracticeModuleProgress;
import com.lms.videocourse.entity.VideoStep;
import com.lms.videocourse.entity.enums.ModuleType;
import com.lms.videocourse.entity.enums.ProgressStatus;
import com.lms.videocourse.repository.ConsumedKanjiProgressEventRepository;
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
public class KanjiModuleProgressSyncService {

    private final VideoModuleRepository videoModuleRepository;
    private final VideoPracticeModuleProgressRepository practiceProgressRepository;
    private final ConsumedKanjiProgressEventRepository consumedEventRepository;
    private final VideoStepRepository videoStepRepository;
    private final IVideoProgressService videoProgressService;

    @Transactional
    public void syncKanjiProgress(KanjiProgressEvent event) {
        if (event == null || event.getEventId() == null || event.getUserId() == null) {
            log.warn("Invalid kanji progress event, skipping: {}", event);
            return;
        }

        if (event.getStudySetId() == null || event.getStudySetId().isBlank()) {
            log.warn("Kanji event {} missing studySetId, skipping", event.getEventId());
            return;
        }

        if (consumedEventRepository.existsById(event.getEventId())) {
            log.info("Kanji event {} already consumed, skipping", event.getEventId());
            return;
        }

        if (!Boolean.TRUE.equals(event.getCompleted())) {
            log.debug("Kanji event {} is not completion, marking consumed", event.getEventId());
            markEventConsumed(event);
            return;
        }

        List<VideoModule> linkedModules = videoModuleRepository
                .findByContentSetIdAndModuleTypeAndIsActiveTrue(event.getStudySetId(), ModuleType.KANJI_ORIGIN);

        if (linkedModules.isEmpty()) {
            log.debug("No active KANJI_ORIGIN modules found for study set {}", event.getStudySetId());
            markEventConsumed(event);
            return;
        }

        Instant completeTime = event.getOccurredAt() != null ? event.getOccurredAt() : Instant.now();

        for (VideoModule module : linkedModules) {
            VideoPracticeModuleProgress progress = practiceProgressRepository
                    .findByUserIdAndVideoModuleId(event.getUserId(), module.getId())
                    .orElseGet(() -> VideoPracticeModuleProgress.builder()
                            .userId(event.getUserId())
                            .videoModuleId(module.getId())
                            .stepId(module.getStepId())
                            .studySetId(getCourseStudySetId(module.getStepId()))
                            .contentSetId(event.getStudySetId())
                            .moduleType(ModuleType.KANJI_ORIGIN)
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
            log.info("Marked KANJI_ORIGIN module {} COMPLETE for user {}", module.getId(), event.getUserId());

            try {
                videoProgressService.getStepProgress(event.getUserId(), module.getStepId());

                VideoStep step = videoStepRepository.findById(module.getStepId()).orElse(null);
                if (step != null) {
                    videoProgressService.getCourseProgress(event.getUserId(), step.getStudySetId());
                }
            } catch (Exception ex) {
                log.error("Failed to roll up progress for KANJI_ORIGIN module {}", module.getId(), ex);
            }
        }

        markEventConsumed(event);
    }

    private void markEventConsumed(KanjiProgressEvent event) {
        consumedEventRepository.save(ConsumedKanjiProgressEvent.builder()
                .eventId(event.getEventId())
                .userId(event.getUserId())
                .studySetId(event.getStudySetId())
                .consumedAt(Instant.now())
                .build());
    }

    private String getCourseStudySetId(String stepId) {
        return videoStepRepository.findById(stepId)
                .map(VideoStep::getStudySetId)
                .orElse(null);
    }
}
