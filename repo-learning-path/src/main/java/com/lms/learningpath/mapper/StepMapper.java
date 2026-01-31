package com.lms.learningpath.mapper;

import com.lms.learningpath.dto.request.CreateStepRequest;
import com.lms.learningpath.dto.request.UpdateStepRequest;
import com.lms.learningpath.dto.response.StepResponse;
import com.lms.learningpath.entity.Step;
import org.springframework.stereotype.Component;

@Component
public class StepMapper {

    public Step toEntity(CreateStepRequest request) {
        return Step.builder()
                .learningPathId(request.getLearningPathId())
                .title(request.getTitle())
                .description(request.getDescription())
                .stepOrder(request.getStepOrder())
                .icon(request.getIcon())
                .color(request.getColor())
                .estimatedMinutes(request.getEstimatedMinutes())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .isActive(true)
                .build();
    }

    public void updateEntity(Step entity, UpdateStepRequest request) {
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getStepOrder() != null) {
            entity.setStepOrder(request.getStepOrder());
        }
        if (request.getIcon() != null) {
            entity.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            entity.setColor(request.getColor());
        }
        if (request.getEstimatedMinutes() != null) {
            entity.setEstimatedMinutes(request.getEstimatedMinutes());
        }
        if (request.getIsRequired() != null) {
            entity.setIsRequired(request.getIsRequired());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
    }

    public StepResponse toResponse(Step entity) {
        return StepResponse.builder()
                .id(entity.getId())
                .learningPathId(entity.getLearningPathId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .stepOrder(entity.getStepOrder())
                .icon(entity.getIcon())
                .color(entity.getColor())
                .estimatedMinutes(entity.getEstimatedMinutes())
                .isRequired(entity.getIsRequired())
                .isActive(entity.getIsActive())
                .createdDate(entity.getCreatedAt())
                .lastModifiedDate(entity.getUpdatedAt())
                .build();
    }
}
