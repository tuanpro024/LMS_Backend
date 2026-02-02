package com.lms.learningpath.service.impl;

import com.lms.learningpath.exception.ResourceAlreadyExistsException;
import com.lms.learningpath.exception.ResourceNotFoundException;
import com.lms.learningpath.dto.request.CreateStepRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.request.UpdateStepRequest;
import com.lms.learningpath.dto.response.StepResponse;
import com.lms.learningpath.entity.Step;
import com.lms.learningpath.entity.StepProgress;
import com.lms.learningpath.entity.StepUnlockRule;
import com.lms.learningpath.mapper.StepMapper;
import com.lms.learningpath.repository.*;
import com.lms.learningpath.service.IStepService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepServiceImpl implements IStepService {

    private final StepRepository stepRepository;
    private final StepProgressRepository stepProgressRepository;
    private final StepUnlockRuleRepository stepUnlockRuleRepository;
    private final StepModuleRepository stepModuleRepository;
    private final StepMapper stepMapper;

    @Override
    @Transactional
    public StepResponse createStep(CreateStepRequest request, String userId) {
        log.info("Creating step: {} for learning path: {}", request.getTitle(), request.getLearningPathId());

        // Check if step order already exists
        if (stepRepository.existsByLearningPathIdAndStepOrder(
                request.getLearningPathId(), request.getStepOrder())) {
            throw new ResourceAlreadyExistsException(
                    "Step with order " + request.getStepOrder() + " already exists");
        }

        Step step = stepMapper.toEntity(request);
        step = stepRepository.save(step);

        // Create unlock rule for sequential unlocking
        createUnlockRuleForStep(step);

        log.info("Successfully created step: {}", step.getId());
        return stepMapper.toResponse(step);
    }

    @Override
    public StepResponse getStepById(String id) {
        Step step = findStepById(id);
        StepResponse response = stepMapper.toResponse(step);

        // Add module count
        long totalModules = stepModuleRepository.countByStepId(id);
        response.setTotalModules((int) totalModules);

        return response;
    }

    @Override
    public StepResponse getStepWithProgress(String id, String userId) {
        Step step = findStepById(id);
        StepResponse response = stepMapper.toResponse(step);

        // Add module count
        long totalModules = stepModuleRepository.countByStepId(id);
        response.setTotalModules((int) totalModules);

        // Add user progress
        Optional<StepProgress> progressOpt = stepProgressRepository.findByUserIdAndStepId(userId, id);
        if (progressOpt.isPresent()) {
            StepProgress progress = progressOpt.get();
            response.setCompletedModules(progress.getCompletedModules());
            response.setProgressPercentage(progress.getProgressPercentage());
        }

        // Check if unlocked
        response.setIsUnlocked(isStepUnlocked(userId, id));

        return response;
    }

    @Override
    public List<StepResponse> getStepsByLearningPathId(String learningPathId) {
        List<Step> steps = stepRepository
                .findByLearningPathIdAndIsActiveTrueOrderByStepOrderAsc(learningPathId);

        return steps.stream()
                .map(step -> {
                    StepResponse response = stepMapper.toResponse(step);
                    long totalModules = stepModuleRepository.countByStepId(step.getId());
                    response.setTotalModules((int) totalModules);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<StepResponse> getStepsByLearningPathIdWithProgress(String learningPathId, String userId) {
        List<Step> steps = stepRepository
                .findByLearningPathIdAndIsActiveTrueOrderByStepOrderAsc(learningPathId);

        return steps.stream()
                .map(step -> {
                    StepResponse response = stepMapper.toResponse(step);
                    long totalModules = stepModuleRepository.countByStepId(step.getId());
                    response.setTotalModules((int) totalModules);

                    // Add user progress
                    stepProgressRepository.findByUserIdAndStepId(userId, step.getId())
                            .ifPresent(progress -> {
                                response.setCompletedModules(progress.getCompletedModules());
                                response.setProgressPercentage(progress.getProgressPercentage());
                            });

                    // Check if unlocked
                    response.setIsUnlocked(isStepUnlocked(userId, step.getId()));

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StepResponse updateStep(String id, UpdateStepRequest request, String userId) {
        log.info("Updating step: {}", id);

        Step step = findStepById(id);
        stepMapper.updateEntity(step, request);
        step = stepRepository.save(step);

        log.info("Successfully updated step: {}", id);
        return stepMapper.toResponse(step);
    }

    @Override
    @Transactional
    public void deleteStep(String id, String userId) {
        log.info("Deleting step: {}", id);

        Step step = findStepById(id);
        step.setIsActive(false);
        stepRepository.save(step);

        log.info("Successfully soft-deleted step: {}", id);
    }

    @Override
    @Transactional
    public void reorderSteps(String learningPathId, ReorderItemsRequest request) {
        log.info("Reordering steps for learning path: {}", learningPathId);

        for (ReorderItemsRequest.ReorderItem item : request.getItems()) {
            Step step = findStepById(item.getId());
            step.setStepOrder(item.getNewOrder());
            stepRepository.save(step);

            // Update unlock rules when reordering
            updateUnlockRuleForStep(step);
        }

        log.info("Successfully reordered {} steps", request.getItems().size());
    }

    private void createUnlockRuleForStep(Step step) {
        // Find previous step (stepOrder - 1)
        Optional<Step> previousStepOpt = stepRepository
                .findByLearningPathIdAndStepOrder(step.getLearningPathId(), step.getStepOrder() - 1);

        StepUnlockRule unlockRule = StepUnlockRule.builder()
                .stepId(step.getId())
                .requiredStepId(previousStepOpt.map(Step::getId).orElse(null))
                .requireAllModules(true)
                .isActive(true)
                .build();

        stepUnlockRuleRepository.save(unlockRule);
        log.info("Created unlock rule for step: {} requiring previous step: {}",
                step.getId(), unlockRule.getRequiredStepId());
    }

    private void updateUnlockRuleForStep(Step step) {
        // Find and update existing unlock rule
        stepUnlockRuleRepository.findByStepId(step.getId())
                .ifPresent(unlockRule -> {
                    Optional<Step> previousStepOpt = stepRepository
                            .findByLearningPathIdAndStepOrder(step.getLearningPathId(), step.getStepOrder() - 1);

                    unlockRule.setRequiredStepId(previousStepOpt.map(Step::getId).orElse(null));
                    stepUnlockRuleRepository.save(unlockRule);
                });
    }

    private boolean isStepUnlocked(String userId, String stepId) {
        // Get unlock rule
        Optional<StepUnlockRule> unlockRuleOpt = stepUnlockRuleRepository
                .findByStepIdAndIsActiveTrue(stepId);

        if (unlockRuleOpt.isEmpty()) {
            return true; // No rule = always unlocked
        }

        StepUnlockRule rule = unlockRuleOpt.get();

        // If no required step = first step = always unlocked
        if (rule.getRequiredStepId() == null) {
            return true;
        }

        // Check if required step is completed
        Optional<StepProgress> progressOpt = stepProgressRepository
                .findByUserIdAndStepId(userId, rule.getRequiredStepId());

        return progressOpt.map(StepProgress::canUnlockNext).orElse(false);
    }

    private Step findStepById(String id) {
        return stepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found with id: " + id));
    }
}
