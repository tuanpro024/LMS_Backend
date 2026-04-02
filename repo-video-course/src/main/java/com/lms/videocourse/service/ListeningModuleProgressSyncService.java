package com.lms.videocourse.service;

import com.lms.videocourse.dto.event.ListeningProgressEvent;
import com.lms.videocourse.entity.*;
import com.lms.videocourse.entity.enums.ModuleType;
import com.lms.videocourse.entity.enums.ProgressStatus;
import com.lms.videocourse.repository.ConsumedListeningProgressEventRepository;
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
public class ListeningModuleProgressSyncService {

    private final ConsumedListeningProgressEventRepository consumedEventRepository;
    private final VideoModuleRepository videoModuleRepository;
    private final VideoPracticeModuleProgressRepository practiceProgressRepository;
    private final IVideoProgressService videoProgressService;
    private final VideoStepRepository videoStepRepository;

    @Transactional
    public void syncProgress(ListeningProgressEvent event) {
        if (event.getEventId() == null || consumedEventRepository.existsById(event.getEventId())) {
            log.info("Event {} already consumed or invalid", event.getEventId());
            return;
        }

        if (!event.isCompleted()) {
            log.info("Event {} is not COMPLETED, skipping for phase 1", event.getEventId());
            saveConsumedEvent(event);
            return;
        }

        List<VideoModule> modules = videoModuleRepository.findByContentSetIdAndModuleTypeAndIsActiveTrue(
                event.getStudySetId(), ModuleType.LISTENING);

        if (modules.isEmpty()) {
            log.info("No active listening video modules found for studySetId {}", event.getStudySetId());
            saveConsumedEvent(event);
            return;
        }

        for (VideoModule module : modules) {
            VideoPracticeModuleProgress progress = practiceProgressRepository
                    .findByUserIdAndVideoModuleId(event.getUserId(), module.getId())
                    .orElseGet(() -> VideoPracticeModuleProgress.builder()
                            .userId(event.getUserId())
                            .videoModuleId(module.getId())
                            .stepId(module.getStepId())
                            .studySetId(getCourseStudySetId(module.getStepId()))
                            .moduleType(ModuleType.LISTENING)
                            .contentSetId(event.getStudySetId())
                            .progressPercentage(0)
                            .firstStartedAt(Instant.now())
                            .build());

            progress.setProgressPercentage(event.getProgressPercentage());
            progress.setStatus(ProgressStatus.COMPLETED);
            progress.setCompletedAt(event.getCompletedAt() != null ? event.getCompletedAt() : Instant.now());
            progress.setLastSyncedEventId(event.getEventId());
            progress.setLastSyncedAt(Instant.now());

            practiceProgressRepository.save(progress);

            // trigger rollup
            videoProgressService.getStepProgress(event.getUserId(), module.getStepId());
            videoProgressService.getCourseProgress(event.getUserId(), getCourseStudySetId(module.getStepId()));
        }

        saveConsumedEvent(event);
    }

    private String getCourseStudySetId(String stepId) {
        return videoStepRepository.findById(stepId)
                .map(VideoStep::getStudySetId)
                .orElse(null);
    }

    private void saveConsumedEvent(ListeningProgressEvent event) {
        if (event.getEventId() != null) {
            consumedEventRepository.save(ConsumedListeningProgressEvent.builder()
                    .eventId(event.getEventId())
                    .userId(event.getUserId())
                    .studySetId(event.getStudySetId())
                    .consumedAt(Instant.now())
                    .build());
        }
    }
}
