package com.lms.learningpath.service.impl;

import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.UpdateProgressRequest;
import com.lms.learningpath.dto.response.LearningPathProgressResponse;
import com.lms.learningpath.dto.response.ModuleProgressDto;
import com.lms.learningpath.dto.response.StepProgressResponse;
import com.lms.learningpath.entity.ConsumedFlashcardProgressEvent;
import com.lms.learningpath.entity.ConsumedQuizProgressEvent;
import com.lms.learningpath.entity.*;
import com.lms.learningpath.entity.enums.ModuleType;
import com.lms.learningpath.entity.enums.ProgressStatus;
import com.lms.learningpath.exception.ResourceNotFoundException;
import com.lms.learningpath.integration.event.FlashcardProgressEvent;
import com.lms.learningpath.integration.event.QuizProgressEvent;
import com.lms.learningpath.mapper.LearningPathProgressMapper;
import com.lms.learningpath.mapper.StepProgressMapper;
import com.lms.learningpath.repository.*;
import com.lms.learningpath.service.IProgressTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        private final ConsumedQuizProgressEventRepository consumedQuizProgressEventRepository;
        private final ConsumedFlashcardProgressEventRepository consumedFlashcardProgressEventRepository;
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
                                                        .totalItems(1)
                                                        .score(0)
                                                        .totalAttempts(0)
                                                        .firstStartedAt(Instant.now())
                                                        .startedAt(Instant.now())
                                                        .build();
                                        return moduleProgressRepository.save(newProgress);
                                });

                if (progress.getTotalItems() == null || progress.getTotalItems() <= 0) {
                        progress.setTotalItems(1);
                        progress = moduleProgressRepository.save(progress);
                }

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

                if (request.getScore() != null) {
                        progress.setScore(request.getScore().intValue());
                        progress.setTotalAttempts(progress.getTotalAttempts() + 1);
                }

                if (request.getCompletedItems() != null) {
                        progress.setCompletedItems(request.getCompletedItems());
                }

                if (request.getStudyTimeSeconds() != null) {
                        progress.setStudyTimeSeconds(request.getStudyTimeSeconds());
                }

                if (request.getMetadata() != null) {
                        progress.setMetadata(request.getMetadata());
                }

                if (progress.getStatus() == ProgressStatus.NOT_STARTED) {
                        progress.setStatus(ProgressStatus.IN_PROGRESS);
                }

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

                boolean alreadyCompleted = progress.getStatus() == ProgressStatus.COMPLETED;
                boolean quizPassed = module.getModuleType() != ModuleType.QUIZ
                                || Boolean.TRUE.equals(request.getPassed());

                if (alreadyCompleted || quizPassed) {
                        progress.setStatus(ProgressStatus.COMPLETED);
                        progress.setCompletedItems(1);
                        if (progress.getTotalItems() == null || progress.getTotalItems() <= 0) {
                                progress.setTotalItems(1);
                        }
                        if (progress.getCompletedAt() == null) {
                                progress.setCompletedAt(Instant.now());
                        }
                } else {
                        progress.setStatus(ProgressStatus.IN_PROGRESS);
                        if (progress.getTotalItems() == null || progress.getTotalItems() <= 0) {
                                progress.setTotalItems(1);
                        }
                        progress.setCompletedItems(0);
                }

                progress.setScore(request.getScore() != null ? request.getScore().intValue() : null);
                progress.setTotalAttempts(progress.getTotalAttempts() + 1);
                progress.setStudyTimeSeconds(request.getTotalStudyTimeSeconds() != null
                                ? request.getTotalStudyTimeSeconds()
                                : progress.getStudyTimeSeconds());
                if (request.getMetadata() != null) {
                        progress.setMetadata(request.getMetadata());
                }
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
                                .findByStudySetIdAndIsActiveTrueOrderByCreatedAtAsc(studySetId);

                return learningPaths.stream()
                                .map(lp -> learningPathProgressRepository
                                                .findByUserIdAndLearningPathId(userId, lp.getId())
                                                .map(learningPathProgressMapper::toResponse)
                                                .orElse(null))
                                .filter(p -> p != null)
                                .collect(Collectors.toList());
        }

        @Transactional
        public void syncQuizProgressEvent(QuizProgressEvent event) {
                if (event == null
                                || event.eventId() == null
                                || event.eventId().isBlank()
                                || event.userId() == null
                                || event.userId().isBlank()
                                || event.studySetId() == null
                                || event.studySetId().isBlank()) {
                        return;
                }

                if (consumedQuizProgressEventRepository.existsByEventId(event.eventId())) {
                        log.debug("Skip duplicated quiz progress event {}", event.eventId());
                        return;
                }

                List<StepModule> quizModules = stepModuleRepository
                                .findByModuleTypeAndContentSetIdAndIsActiveTrue(ModuleType.QUIZ, event.studySetId());

                if (quizModules.isEmpty()) {
                        consumedQuizProgressEventRepository.save(ConsumedQuizProgressEvent.builder()
                                        .eventId(event.eventId())
                                        .consumedAt(Instant.now())
                                        .build());
                        log.debug("No quiz module matched studySet {} for event {}", event.studySetId(),
                                        event.eventId());
                        return;
                }

                Instant now = Instant.now();
                Integer score = event.scorePercentage() == null ? null : (int) Math.round(event.scorePercentage());

                Set<String> touchedStepIds = new HashSet<>();
                for (StepModule module : quizModules) {
                        ModuleProgress progress = moduleProgressRepository
                                        .findByUserIdAndStepModuleId(event.userId(), module.getId())
                                        .orElseGet(() -> ModuleProgress.builder()
                                                        .userId(event.userId())
                                                        .stepModuleId(module.getId())
                                                        .stepId(module.getStepId())
                                                        .status(ProgressStatus.NOT_STARTED)
                                                        .completedItems(0)
                                                        .totalItems(1)
                                                        .score(0)
                                                        .totalAttempts(0)
                                                        .studyTimeSeconds(0)
                                                        .firstStartedAt(now)
                                                        .startedAt(now)
                                                        .build());

                        if (Boolean.TRUE.equals(event.passed())) {
                                progress.setStatus(ProgressStatus.COMPLETED);
                                progress.setCompletedItems(1);
                                if (progress.getCompletedAt() == null) {
                                        progress.setCompletedAt(now);
                                }
                        } else {
                                if (progress.getStatus() == ProgressStatus.NOT_STARTED) {
                                        progress.setStatus(ProgressStatus.IN_PROGRESS);
                                }
                                progress.setCompletedItems(0);
                        }

                        progress.setTotalItems(1);
                        progress.setScore(score);
                        progress.setTotalAttempts(
                                        (progress.getTotalAttempts() == null ? 0 : progress.getTotalAttempts()) +
                                                        1);
                        if (event.timeTakenSeconds() != null && event.timeTakenSeconds() > 0) {
                                progress.setStudyTimeSeconds(
                                                (progress.getStudyTimeSeconds() == null ? 0
                                                                : progress.getStudyTimeSeconds())
                                                                + event.timeTakenSeconds());
                        }
                        progress.setLastAttemptAt(now);
                        progress.setMetadata(buildQuizMetadata(event));

                        moduleProgressRepository.save(progress);
                        touchedStepIds.add(module.getStepId());
                }

                for (String stepId : touchedStepIds) {
                        updateStepProgress(event.userId(), stepId);
                        Step step = stepRepository.findById(stepId).orElse(null);
                        if (step != null) {
                                updateLearningPathProgress(event.userId(), step.getLearningPathId());
                        }
                }

                consumedQuizProgressEventRepository.save(ConsumedQuizProgressEvent.builder()
                                .eventId(event.eventId())
                                .consumedAt(now)
                                .build());

                log.info("Synced quiz progress event {} for user {} and {} module(s)",
                                event.eventId(), event.userId(), quizModules.size());
        }

        @Transactional
        public void syncFlashcardProgressEvent(FlashcardProgressEvent event) {
                if (event == null
                                || event.eventId() == null
                                || event.eventId().isBlank()
                                || event.userId() == null
                                || event.userId().isBlank()
                                || event.studySetId() == null
                                || event.studySetId().isBlank()) {
                        return;
                }

                if (consumedFlashcardProgressEventRepository.existsByEventId(event.eventId())) {
                        log.debug("Skip duplicated flashcard progress event {}", event.eventId());
                        return;
                }

                List<StepModule> flashcardModules = stepModuleRepository
                                .findByModuleTypeAndContentSetIdAndIsActiveTrue(ModuleType.FLASHCARD,
                                                event.studySetId());

                if (flashcardModules.isEmpty()) {
                        consumedFlashcardProgressEventRepository.save(ConsumedFlashcardProgressEvent.builder()
                                        .eventId(event.eventId())
                                        .consumedAt(Instant.now())
                                        .build());
                        log.debug("No flashcard module matched studySet {} for event {}", event.studySetId(),
                                        event.eventId());
                        return;
                }

                Instant now = Instant.now();
                Integer learnedCards = event.learnedCards() == null ? 0 : Math.max(event.learnedCards(), 0);
                Integer totalCards = event.totalCards() == null ? 0 : Math.max(event.totalCards(), 0);
                Integer score = event.progressPercentage() == null ? null
                                : (int) Math.round(event.progressPercentage());

                Set<String> touchedStepIds = new HashSet<>();
                for (StepModule module : flashcardModules) {
                        ModuleProgress progress = moduleProgressRepository
                                        .findByUserIdAndStepModuleId(event.userId(), module.getId())
                                        .orElseGet(() -> ModuleProgress.builder()
                                                        .userId(event.userId())
                                                        .stepModuleId(module.getId())
                                                        .stepId(module.getStepId())
                                                        .status(ProgressStatus.NOT_STARTED)
                                                        .completedItems(0)
                                                        .totalItems(totalCards)
                                                        .score(0)
                                                        .totalAttempts(0)
                                                        .studyTimeSeconds(0)
                                                        .firstStartedAt(now)
                                                        .startedAt(now)
                                                        .build());

                        if (Boolean.TRUE.equals(event.completed())) {
                                progress.setStatus(ProgressStatus.COMPLETED);
                                progress.setCompletedItems(totalCards);
                                if (progress.getCompletedAt() == null) {
                                        progress.setCompletedAt(now);
                                }
                        } else if (learnedCards > 0) {
                                progress.setStatus(ProgressStatus.IN_PROGRESS);
                                progress.setCompletedItems(learnedCards);
                                progress.setCompletedAt(null);
                        } else {
                                progress.setStatus(ProgressStatus.NOT_STARTED);
                                progress.setCompletedItems(0);
                                progress.setCompletedAt(null);
                        }

                        progress.setTotalItems(totalCards);
                        progress.setScore(score);
                        progress.setTotalAttempts(
                                        (progress.getTotalAttempts() == null ? 0 : progress.getTotalAttempts())
                                                        + 1);
                        progress.setLastAttemptAt(now);
                        progress.setMetadata(buildFlashcardMetadata(event));

                        moduleProgressRepository.save(progress);
                        touchedStepIds.add(module.getStepId());
                }

                for (String stepId : touchedStepIds) {
                        updateStepProgress(event.userId(), stepId);
                        Step step = stepRepository.findById(stepId).orElse(null);
                        if (step != null) {
                                updateLearningPathProgress(event.userId(), step.getLearningPathId());
                        }
                }

                consumedFlashcardProgressEventRepository.save(ConsumedFlashcardProgressEvent.builder()
                                .eventId(event.eventId())
                                .consumedAt(now)
                                .build());

                log.info("Synced flashcard progress event {} for user {} and {} module(s)",
                                event.eventId(), event.userId(), flashcardModules.size());
        }

        private String buildQuizMetadata(QuizProgressEvent event) {
                String attemptId = event.attemptId() == null ? "" : event.attemptId();
                String quizId = event.quizId() == null ? "" : event.quizId();
                String passed = Boolean.TRUE.equals(event.passed()) ? "true" : "false";
                String score = event.scorePercentage() == null ? "" : String.valueOf(event.scorePercentage());
                return String.format(
                                "{\"source\":\"repo-quiz\",\"eventId\":\"%s\",\"attemptId\":\"%s\",\"quizId\":\"%s\",\"passed\":%s,\"scorePercentage\":%s}",
                                event.eventId(), attemptId, quizId, passed, score);
        }

        private String buildFlashcardMetadata(FlashcardProgressEvent event) {
                String completed = Boolean.TRUE.equals(event.completed()) ? "true" : "false";
                String learnedCards = event.learnedCards() == null ? "0" : String.valueOf(event.learnedCards());
                String totalCards = event.totalCards() == null ? "0" : String.valueOf(event.totalCards());
                String progressPercentage = event.progressPercentage() == null ? "null"
                                : String.valueOf(event.progressPercentage());
                return String.format(
                                "{\"source\":\"repo-flashcard\",\"eventId\":\"%s\",\"completed\":%s,\"learnedCards\":%s,\"totalCards\":%s,\"progressPercentage\":%s}",
                                event.eventId(), completed, learnedCards, totalCards, progressPercentage);
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
                                        StepModule sm = stepModuleRepository.findById(mp.getStepModuleId())
                                                        .orElse(null);
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
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Learning path not found: " + learningPathId));

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
                                                .studySetId(learningPath.getStudySet().getId())
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
                                .completedItems(progress.getCompletedItems())
                                .totalItems(progress.getTotalItems())
                                .progressPercentage(progress.getProgressPercentage())
                                .score(progress.getScore())
                                .totalAttempts(progress.getTotalAttempts())
                                .studyTimeSeconds(progress.getStudyTimeSeconds())
                                .firstStartedAt(progress.getFirstStartedAt())
                                .lastAttemptAt(progress.getLastAttemptAt())
                                .startedAt(progress.getStartedAt())
                                .completedAt(progress.getCompletedAt())
                                .build();
        }
}
