package com.lms.learningpath.mapper;

import com.lms.learningpath.dto.request.CreateLearningPathRequest;
import com.lms.learningpath.dto.request.UpdateLearningPathRequest;
import com.lms.learningpath.dto.response.LearningPathResponse;
import com.lms.learningpath.entity.LearningPath;
import org.springframework.stereotype.Component;

@Component
public class LearningPathMapper {

    public LearningPath toEntity(CreateLearningPathRequest request, String userId) {
        return LearningPath.builder()
                .studySetId(request.getStudySetId())
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnail(request.getThumbnail())
                .displayOrder(request.getDisplayOrder())
                .estimatedHours(request.getEstimatedHours())
                .level(request.getLevel())
                .isActive(true)
                .createdBy(userId)
                .build();
    }

    public void updateEntity(LearningPath entity, UpdateLearningPathRequest request) {
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getThumbnail() != null) {
            entity.setThumbnail(request.getThumbnail());
        }
        if (request.getDisplayOrder() != null) {
            entity.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getEstimatedHours() != null) {
            entity.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getLevel() != null) {
            entity.setLevel(request.getLevel());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
    }

    public LearningPathResponse toResponse(LearningPath entity) {
        return LearningPathResponse.builder()
                .id(entity.getId())
                .studySetId(entity.getStudySetId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .thumbnail(entity.getThumbnail())
                .displayOrder(entity.getDisplayOrder())
                .estimatedHours(entity.getEstimatedHours())
                .level(entity.getLevel())
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedAt())
                .lastModifiedDate(entity.getUpdatedAt())
                .build();
    }
}
