package com.lms.learningpath.mapper;

import com.lms.learningpath.dto.response.LearningPathProgressResponse;
import com.lms.learningpath.entity.LearningPathProgress;
import org.springframework.stereotype.Component;

@Component
public class LearningPathProgressMapper {

    public LearningPathProgressResponse toResponse(LearningPathProgress entity) {
        return LearningPathProgressResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .learningPathId(entity.getLearningPathId())
                .studySetId(entity.getStudySetId())
                .status(entity.getStatus())
                .completedSteps(entity.getCompletedSteps())
                .totalSteps(entity.getTotalSteps())
                .currentStepId(entity.getCurrentStepId())
                .progressPercentage(entity.getProgressPercentage())
                .firstStartedAt(entity.getFirstStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
