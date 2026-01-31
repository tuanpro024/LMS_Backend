package com.lms.learningpath.service.impl;

import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.UpdateProgressRequest;
import com.lms.learningpath.dto.response.LearningPathProgressResponse;
import com.lms.learningpath.dto.response.ModuleProgressDto;
import com.lms.learningpath.dto.response.StepProgressResponse;
import com.lms.learningpath.entity.*;
import com.lms.learningpath.entity.enums.ProgressStatus;
import com.lms.learningpath.exception.ResourceNotFoundException;
import com.lms.learningpath.mapper.LearningPathProgressMapper;
import com.lms.learningpath.mapper.StepProgressMapper;
import com.lms.learningpath.repository.*;
import com.lms.learningpath.service.IProgressTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of progress tracking service for step-based hierarchy.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressTrackingServiceImpl implements IProgressTrackingService {

    private final StepModuleRepository stepModuleRepository;
    private final ModuleProgressRepository moduleProgressRepository;
    private final StepProgressRepository stepProgressRepository;
    private final LearningPathProgressRepository learningPathProgressRepository;
    private final StepRepository stepRepository;
    private final LearningPathRepository learningPathRepository;
    private final StepProgressMapper stepProgressMapper;
    private final LearningPathProgressMapper learningPathProgressMapper;

    @Override
    @Transactional
    public ModuleProgressDto startModule(String userId, String moduleId) {
        log.info("User {} starting module {}", userId, moduleId);

        StepModule module = stepModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Step module not found: " + moduleId));

        ModuleProgress progress = moduleProgressRepository
                .findByUserIdAndStepModuleId(userId, moduleId)
                .orElseGet(() -> {
                    ModuleProgress newProgress = ModuleProgress.builder()
                            .userId(userId)
                            .stepModuleId(moduleId)
                            .stepId(module.getStepId())
                            .status(ProgressStatus.IN_PROGRESS)
                            .score(0)
                            .totalAttempts(0)
                            .firstStartedAt(Instant.now())
                            .build();
                    return moduleProgressRepository.save(newProgress);
                });

        // Update step progress
        updateStepProgress(userId, module.getStepId());

        return mapToDto(progress);
    }

    @Override
    @Transactional
    public ModuleProgressDto updateProgress(String userId, String moduleId, UpdateProgressRequest request) {
        log.info("Updating progress for user {} on module {}", userId, moduleId);

        ModuleProgress progress = moduleProgressRepository
                .findByUserIdAndStepModuleId(userId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module progress not found"));

        progress.setScore(request.getScore() != null ? request.getScore().intValue() : null);
        progress.setTotalAttempts(progress.getTotalAttempts() + 1);
        progress.setLastAttemptAt(Instant.now());

        progress = moduleProgressRepository.save(progress);

        return mapToDto(progress);
    }

    @Override
    @Transactional
    public ModuleProgressDto completeModule(String userId, String moduleId, CompleteModuleRequest request) {
        log.info("User {} completing module {}", userId, moduleId);

        StepModule module = stepModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Step module not found: " + moduleId));

        ModuleProgress progress = moduleProgressRepository
                .findByUserIdAndStepModuleId(userId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module progress not found"));

        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setScore(request.getScore() != null ? request.getScore().intValue() : null);
        progress.setTotalAttempts(progress.getTotalAttempts() + 1);
        progress.setCompletedAt(Instant.now());
        progress.setLastAttemptAt(Instant.now());

        progress = moduleProgressRepository.save(progress);

        // Update step progress
        updateStepProgress(userId, module.getStepId());

        // Update learning path progress
        Step step = stepRepository.findById(module.getStepId()).orElseThrow();
        updateLearningPathProgress(userId, step.getLearningPathId());

        return mapToDto(progress);
    }

    @Override
    public StepProgressResponse getStepProgress(String userId, String stepId) {
        return stepProgressRepository.findByUserIdAndStepId(userId, stepId)
                .map(stepProgressMapper::toResponse)
                .orElse(null);
    }

    @Override
    public LearningPathProgressResponse getLearningPathProgress(String userId, String learningPathId) {
        return learningPathProgressRepository.findByUserIdAndLearningPathId(userId, learningPathId)
                .map(learningPathProgressMapper::toResponse)
                .orElse(null);
    }

    @Override
    public List<LearningPathProgressResponse> getAllLearningPathProgress(String userId, String studySetId) {
        List<LearningPath> learningPaths = learningPathRepository
                .findByStudySetIdAndIsActiveTrueOrderByDisplayOrderAsc(studySetId);

        return learningPaths.stream()
                .map(lp -> learningPathProgressRepository
                        .findByUserIdAndLearningPathId(userId, lp.getId())
                        .map(learningPathProgressMapper::toResponse)
                        .orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    // ============ Private Helper Methods ============

    private void updateStepProgress(String userId, String stepId) {
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found: " + stepId));

        // Get all modules for this step
        List<StepModule> allModules = stepModuleRepository
                .findByStepIdAndIsActiveTrueOrderByModuleOrderAsc(stepId);

        int totalModules = allModules.size();
        long totalRequired = allModules.stream().filter(StepModule::getIsRequired).count();

        // Get completed modules
        List<String> moduleIds = allModules.stream()
                .map(StepModule::getId)
                .collect(Collectors.toList());

        List<ModuleProgress> completedModules = moduleProgressRepository
                .findByUserIdAndStepModuleIdIn(userId, moduleIds).stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .collect(Collectors.toList());

        int completedCount = completedModules.size();
        long completedRequiredCount = completedModules.stream()
                .filter(mp -> {
                    StepModule sm = stepModuleRepository.findById(mp.getStepModuleId()).orElse(null);
                    return sm != null && sm.getIsRequired();
                })
                .count();

        // Calculate status
        ProgressStatus status;
        if (completedCount == 0) {
            status = ProgressStatus.NOT_STARTED;
        } else if (completedRequiredCount >= totalRequired) {
            status = ProgressStatus.COMPLETED;
        } else {
            status = ProgressStatus.IN_PROGRESS;
        }

        // Find or create progress
        StepProgress progress = stepProgressRepository
                .findByUserIdAndStepId(userId, stepId)
                .orElseGet(() -> StepProgress.builder()
                        .userId(userId)
                        .stepId(stepId)
                        .learningPathId(step.getLearningPathId())
                        .status(ProgressStatus.NOT_STARTED)
                        .completedModules(0)
                        .totalModules(totalModules)
                        .requiredCompletedModules(0)
                        .totalRequiredModules((int) totalRequired)
                        .firstStartedAt(null)
                        .build());

        // Update progress
        progress.setStatus(status);
        progress.setCompletedModules(completedCount);
        progress.setTotalModules(totalModules);
        progress.setRequiredCompletedModules((int) completedRequiredCount);
        progress.setTotalRequiredModules((int) totalRequired);

        if (progress.getFirstStartedAt() == null && completedCount > 0) {
            progress.setFirstStartedAt(Instant.now());
        }

        if (status == ProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
        }

        stepProgressRepository.save(progress);
    }

    private void updateLearningPathProgress(String userId, String learningPathId) {
        LearningPath learningPath = learningPathRepository.findById(learningPathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found: " + learningPathId));

        // Get all steps
        List<Step> allSteps = stepRepository
                .findByLearningPathIdAndIsActiveTrueOrderByStepOrderAsc(learningPathId);

        int totalSteps = allSteps.size();

        // Get completed steps
        List<String> stepIds = allSteps.stream()
                .map(Step::getId)
                .collect(Collectors.toList());

        long completedSteps = stepProgressRepository.findByUserIdAndStepIdIn(userId, stepIds).stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();

        // Determine status
        ProgressStatus status;
        if (completedSteps == 0) {
            status = ProgressStatus.NOT_STARTED;
        } else if (completedSteps >= totalSteps) {
            status = ProgressStatus.COMPLETED;
        } else {
            status = ProgressStatus.IN_PROGRESS;
        }

        // Get current step (first incomplete)
        String currentStepId = allSteps.stream()
                .filter(s -> {
                    return stepProgressRepository.findByUserIdAndStepId(userId, s.getId())
                            .map(p -> p.getStatus() != ProgressStatus.COMPLETED)
                            .orElse(true);
                })
                .findFirst()
                .map(Step::getId)
                .orElse(null);

        // Find or create progress
        LearningPathProgress progress = learningPathProgressRepository
                .findByUserIdAndLearningPathId(userId, learningPathId)
                .orElseGet(() -> LearningPathProgress.builder()
                        .userId(userId)
                        .learningPathId(learningPathId)
                        .studySetId(learningPath.getStudySetId())
                        .status(ProgressStatus.NOT_STARTED)
                        .completedSteps(0)
                        .totalSteps(totalSteps)
                        .currentStepId(null)
                        .build());

        // Update progress
        progress.setStatus(status);
        progress.setCompletedSteps((int) completedSteps);
        progress.setTotalSteps(totalSteps);
        progress.setCurrentStepId(currentStepId);

        if (progress.getFirstStartedAt() == null && completedSteps > 0) {
            progress.setFirstStartedAt(Instant.now());
        }

        if (status == ProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
        }

        learningPathProgressRepository.save(progress);
    }

    private ModuleProgressDto mapToDto(ModuleProgress progress) {
        return ModuleProgressDto.builder()
                .id(progress.getId())
                .stepModuleId(progress.getStepModuleId())
                .stepId(progress.getStepId())
                .status(progress.getStatus())
                .score(progress.getScore())
                .totalAttempts(progress.getTotalAttempts())
                .firstStartedAt(progress.getFirstStartedAt())
                .lastAttemptAt(progress.getLastAttemptAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }
}
