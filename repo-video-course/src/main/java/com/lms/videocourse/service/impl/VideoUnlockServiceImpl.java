package com.lms.videocourse.service.impl;

import com.lms.videocourse.entity.VideoStep;
import com.lms.videocourse.entity.enums.ProgressStatus;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.VideoStepProgressRepository;
import com.lms.videocourse.repository.VideoStepRepository;
import com.lms.videocourse.repository.VideoStepUnlockRuleRepository;
import com.lms.videocourse.service.IVideoUnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoUnlockServiceImpl implements IVideoUnlockService {

    private final VideoStepRepository videoStepRepository;
    private final VideoStepUnlockRuleRepository unlockRuleRepository;
    private final VideoStepProgressRepository stepProgressRepository;

    @Override
    public boolean isStepUnlocked(String userId, String stepId) {
        VideoStep step = videoStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + stepId));

        // Step 1 (stepOrder == 1) is always unlocked
        if (step.getStepOrder() == 1) {
            return true;
        }

        // Check unlock rule
        return unlockRuleRepository.findByStepIdAndIsActiveTrue(stepId)
                .map(rule -> {
                    if (rule.getRequiredStepId() == null) {
                        return true; // No prerequisite
                    }

                    // Check if the required step is COMPLETED for this user
                    return stepProgressRepository
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

        VideoStep step = videoStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + stepId));

        return unlockRuleRepository.findByStepIdAndIsActiveTrue(stepId)
                .map(rule -> {
                    if (rule.getRequiredStepId() == null) {
                        return "This step is locked";
                    }

                    VideoStep requiredStep = videoStepRepository.findById(rule.getRequiredStepId())
                            .orElse(null);

                    if (requiredStep != null) {
                        return String.format("Complete all modules in '%s' to unlock this step. Video modules require at least 80%% watch time.",
                                requiredStep.getTitle());
                    }

                    return "Complete the previous step to unlock this step";
                })
                .orElse("This step is locked");
    }
}
