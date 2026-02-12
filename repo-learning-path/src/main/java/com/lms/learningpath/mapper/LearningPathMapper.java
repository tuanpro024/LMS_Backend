package com.lms.learningpath.mapper;

import com.lms.learningpath.dto.request.CreateLearningPathRequest;
import com.lms.learningpath.dto.request.UpdateLearningPathRequest;
import com.lms.learningpath.dto.response.LearningPathResponse;
import com.lms.learningpath.entity.LearningPath;
import org.springframework.stereotype.Component;

@Component
public class LearningPathMapper {

    public LearningPath toEntity(CreateLearningPathRequest request, String userId) {
        LearningPath learningPath = new LearningPath();

        // Set LearningPath-specific fields only
        // StudySet will be set by service layer
        learningPath.setTitle(request.getTitle());
        learningPath.setDescription(request.getDescription());
        learningPath.setThumbnail(request.getThumbnail());
        learningPath.setEstimatedHours(request.getEstimatedHours());
        learningPath.setIsActive(true);
        learningPath.setCreatedBy(userId);

        return learningPath;
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

        if (request.getEstimatedHours() != null) {
            entity.setEstimatedHours(request.getEstimatedHours());
        }

        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
    }

    public LearningPathResponse toResponse(LearningPath entity) {
        return LearningPathResponse.builder()
                .id(entity.getId())
                .studySetId(entity.getStudySet().getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .thumbnail(entity.getThumbnail())
                .estimatedHours(entity.getEstimatedHours())
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedAt())
                .lastModifiedDate(entity.getUpdatedAt())
                .build();
    }
}
