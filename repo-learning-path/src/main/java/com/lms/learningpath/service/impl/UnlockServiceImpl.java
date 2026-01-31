package com.lms.learningpath.service.impl;

import com.lms.learningpath.entity.Step;
import com.lms.learningpath.entity.StepProgress;
import com.lms.learningpath.entity.StepUnlockRule;
import com.lms.learningpath.entity.enums.ProgressStatus;
import com.lms.learningpath.exception.ResourceNotFoundException;
import com.lms.learningpath.repository.StepProgressRepository;
import com.lms.learningpath.repository.StepRepository;
import com.lms.learningpath.repository.StepUnlockRuleRepository;
import com.lms.learningpath.service.IUnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of unlock service for step-based sequential unlocking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnlockServiceImpl implements IUnlockService {

    private final StepRepository stepRepository;
    private final StepUnlockRuleRepository unlockRuleRepository;
    private final StepProgressRepository progressRepository;

    @Override
    public boolean isStepUnlocked(String userId, String stepId) {
        // First step is always unlocked
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found: " + stepId));

        if (step.getStepOrder() == 1) {
            return true;
        }

        // Check unlock rule
        return unlockRuleRepository.findByStepIdAndIsActiveTrue(stepId)
                .map(rule -> {
                    if (rule.getRequiredStepId() == null) {
                        return true; // No requirement
                    }

                    // Check if required step is completed
                    return progressRepository
                            .findByUserIdAndStepId(userId, rule.getRequiredStepId())
                            .map(progress -> progress.getStatus() == ProgressStatus.COMPLETED)
                            .orElse(false);
                })
                .orElse(true); // No rule = unlocked
    }

    @Override
    public String getLockReason(String userId, String stepId) {
        if (isStepUnlocked(userId, stepId)) {
            return null;
        }

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found: " + stepId));

        return unlockRuleRepository.findByStepIdAndIsActiveTrue(stepId)
                .map(rule -> {
                    if (rule.getRequiredStepId() == null) {
                        return "This step is locked";
                    }

                    Step requiredStep = stepRepository.findById(rule.getRequiredStepId())
                            .orElse(null);

                    if (requiredStep != null) {
                        return String.format("Complete '%s' to unlock this step",
                                requiredStep.getTitle());
                    }

                    return "Complete the previous step to unlock this step";
                })
                .orElse("This step is locked");
    }
}
