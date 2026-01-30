package com.lms.learningpath.service.impl;

import com.lms.learningpath.dto.response.SetCompletionResult;
import com.lms.learningpath.dto.response.SetProgressResponse;
import com.lms.learningpath.entity.LearningModule;
import com.lms.learningpath.entity.UserModuleProgress;
import com.lms.learningpath.entity.UserSetProgress;
import com.lms.learningpath.entity.enums.EventType;
import com.lms.learningpath.entity.enums.ModuleStatus;
import com.lms.learningpath.entity.enums.SetStatus;
import com.lms.learningpath.repository.LearningModuleRepository;
import com.lms.learningpath.repository.UserModuleProgressRepository;
import com.lms.learningpath.repository.UserSetProgressRepository;
import com.lms.learningpath.service.LearningEventService;
import com.lms.learningpath.service.ProgressTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressTrackingServiceImpl implements ProgressTrackingService {

    private final LearningModuleRepository moduleRepository;
    private final UserModuleProgressRepository moduleProgressRepository;
    private final UserSetProgressRepository setProgressRepository;
    private final LearningEventService eventService;

    @Override
    @Transactional(readOnly = true)
    public SetProgressResponse getSetProgress(String userId, String studySetId) {
        UserSetProgress progress = setProgressRepository
                .findByUserIdAndStudySetId(userId, studySetId)
                .orElse(null);

        // Calculate current counts
        long totalModules = moduleRepository.countByStudySetId(studySetId);
        long requiredTotal = moduleRepository.countRequiredByStudySetId(studySetId);
        long requiredCompleted = moduleProgressRepository
                .countRequiredByStatusAndStudySetId(userId, studySetId, ModuleStatus.COMPLETED);

        if (progress == null) {
            // No progress yet - create default response
            return SetProgressResponse.builder()
                    .studySetId(studySetId)
                    .status(SetStatus.UNLOCKED)  // Default unlocked (will be checked by unlock service)
                    .completedModules(0)
                    .totalModules((int) totalModules)
                    .requiredCompleted(0)
                    .requiredTotal((int) requiredTotal)
                    .earnedExp(0)
                    .canCompleteSet(false)
                    .build();
        }

        boolean canComplete = requiredCompleted >= requiredTotal;

        return SetProgressResponse.builder()
                .studySetId(studySetId)
                .status(progress.getStatus())
                .completedModules(progress.getCompletedModules())
                .totalModules(progress.getTotalModules())
                .requiredCompleted((int) requiredCompleted)
                .requiredTotal((int) requiredTotal)
                .bestScore(progress.getBestScore())
                .earnedExp(progress.getEarnedExp())
                .canCompleteSet(canComplete)
                .completedAt(progress.getCompletedAt())
                .lastStudiedAt(progress.getLastStudiedAt())
                .build();
    }

    @Override
    @Transactional
    public SetProgressResponse updateSetProgress(String userId, String studySetId) {
        UserSetProgress progress = setProgressRepository
                .findByUserIdAndStudySetId(userId, studySetId)
                .orElseGet(() -> {
                    // Create new progress
                    long totalModules = moduleRepository.countByStudySetId(studySetId);

                    UserSetProgress newProgress = UserSetProgress.builder()
                            .userId(userId)
                            .studySetId(studySetId)
                            .status(SetStatus.UNLOCKED)
                            .completedModules(0)
                            .totalModules((int) totalModules)
                            .earnedExp(0)
                            .build();

                    return setProgressRepository.save(newProgress);
                });

        // Update status to IN_PROGRESS if not already completed
        if (progress.getStatus() == SetStatus.UNLOCKED) {
            progress.setStatus(SetStatus.IN_PROGRESS);
        }

        // Update counts
        long totalModules = moduleRepository.countByStudySetId(studySetId);
        long completedModules = moduleProgressRepository
                .countRequiredByStatusAndStudySetId(userId, studySetId, ModuleStatus.COMPLETED);

        progress.setCompletedModules((int) completedModules);
        progress.setTotalModules((int) totalModules);
        progress.setLastStudiedAt(Instant.now());

        // Update best score (average of all required completed modules)
        Double avgScore = moduleProgressRepository.getAverageScoreForRequiredModules(userId, studySetId);
        if (avgScore != null) {
            progress.setBestScore(avgScore.intValue());
        }

        UserSetProgress saved = setProgressRepository.save(progress);

        return getSetProgress(userId, studySetId);
    }

    @Override
    @Transactional
    public SetCompletionResult completeSet(String userId, String studySetId) {
        log.info("Completing study set: {} for user: {}", studySetId, userId);

        UserSetProgress progress = setProgressRepository
                .findByUserIdAndStudySetId(userId, studySetId)
                .orElseThrow(() -> new IllegalStateException("Set progress not found"));

        // Get all completed modules
        List<UserModuleProgress> completedModules = moduleProgressRepository
                .findCompletedModulesByStudySetId(userId, studySetId);

        // Calculate total exp
        int totalExp = completedModules.stream()
                .mapToInt(p -> calculateModuleExp(p.getScore()))
                .sum();

        // Get all modules
        List<LearningModule> allModules = moduleRepository
                .findByStudySetIdOrderByOrderIndexAsc(studySetId);

        List<String> completedModuleIds = completedModules.stream()
                .map(p -> p.getModule().getId())
                .collect(Collectors.toList());

        List<String> skippedModuleIds = allModules.stream()
                .filter(m -> !completedModuleIds.contains(m.getId()))
                .map(LearningModule::getId)
                .collect(Collectors.toList());

        // Update progress
        progress.setStatus(SetStatus.COMPLETED);
        progress.setCompletedAt(Instant.now());
        progress.setEarnedExp(totalExp);

        setProgressRepository.save(progress);

        // Log event
        eventService.logEvent(
                userId,
                EventType.SET_COMPLETED,
                studySetId,
                null,
                progress.getBestScore(),
                null,
                null
        );

        log.info("Set completed: {} with score: {}, exp: {}", studySetId, progress.getBestScore(), totalExp);

        // TODO: Get study set title from content-common service
        String title = "Study Set"; // Placeholder

        return SetCompletionResult.builder()
                .studySetId(studySetId)
                .title(title)
                .finalScore(progress.getBestScore())
                .totalExp(totalExp)
                .completedAt(progress.getCompletedAt())
                .completedModuleIds(completedModuleIds)
                .skippedModuleIds(skippedModuleIds)
                .build();
    }

    private int calculateModuleExp(Integer score) {
        if (score == null) return 0;
        return score / 2;  // Same as module completion exp
    }
}