package com.lms.learningpath.mapper;

import com.lms.learningpath.dto.request.CreateUnlockRuleRequest;
import com.lms.learningpath.dto.request.UpdateUnlockRuleRequest;
import com.lms.learningpath.dto.response.UnlockRuleResponse;
import com.lms.learningpath.entity.StudySetUnlockRule;

/**
 * Mapper utility for converting between StudySetUnlockRule entity and DTOs.
 */
public class UnlockRuleMapper {

    private UnlockRuleMapper() {
        // Utility class, prevent instantiation
    }

    /**
     * Convert StudySetUnlockRule entity to UnlockRuleResponse DTO.
     *
     * @param entity the unlock rule entity
     * @return the response DTO
     */
    public static UnlockRuleResponse toResponse(StudySetUnlockRule entity) {
        if (entity == null) {
            return null;
        }

        return UnlockRuleResponse.builder()
                .id(entity.getId())
                .studySetId(entity.getStudySetId())
                .requiredStudySetId(entity.getRequiredStudySetId())
                .requirePreviousInFolder(entity.getRequirePreviousInFolder())
                .requireAllRequiredModules(entity.getRequireAllRequiredModules())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convert CreateUnlockRuleRequest to StudySetUnlockRule entity.
     *
     * @param request the create request
     * @return the entity
     */
    public static StudySetUnlockRule toEntity(CreateUnlockRuleRequest request) {
        if (request == null) {
            return null;
        }

        return StudySetUnlockRule.builder()
                .studySetId(request.getStudySetId())
                .requiredStudySetId(request.getRequiredStudySetId())
                .requirePreviousInFolder(request.getRequirePreviousInFolder())
                .requireAllRequiredModules(request.getRequireAllRequiredModules())
                .isActive(true)
                .build();
    }

    /**
     * Update entity with values from UpdateUnlockRuleRequest.
     * Only updates non-null fields.
     *
     * @param entity  the entity to update
     * @param request the update request
     */
    public static void updateEntity(StudySetUnlockRule entity, UpdateUnlockRuleRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getRequiredStudySetId() != null) {
            entity.setRequiredStudySetId(request.getRequiredStudySetId());
        }
        if (request.getRequirePreviousInFolder() != null) {
            entity.setRequirePreviousInFolder(request.getRequirePreviousInFolder());
        }
        if (request.getRequireAllRequiredModules() != null) {
            entity.setRequireAllRequiredModules(request.getRequireAllRequiredModules());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
    }
}
