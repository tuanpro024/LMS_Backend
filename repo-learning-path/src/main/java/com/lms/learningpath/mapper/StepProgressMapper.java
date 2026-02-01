package com.lms.learningpath.mapper;

import com.lms.learningpath.dto.response.StepProgressResponse;
import com.lms.learningpath.entity.StepProgress;
import org.springframework.stereotype.Component;

@Component
public class StepProgressMapper {

    public StepProgressResponse toResponse(StepProgress entity) {
        return StepProgressResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .stepId(entity.getStepId())
                .learningPathId(entity.getLearningPathId())
                .status(entity.getStatus())
                .completedModules(entity.getCompletedModules())
                .totalModules(entity.getTotalModules())
                .requiredCompletedModules(entity.getRequiredCompletedModules())
                .totalRequiredModules(entity.getTotalRequiredModules())
                .progressPercentage(entity.getProgressPercentage())
                .canUnlockNext(entity.canUnlockNext())
                .firstStartedAt(entity.getFirstStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
