package com.lms.learningpath.mapper;

import com.lms.learningpath.dto.request.AddModuleToStepRequest;
import com.lms.learningpath.dto.response.StepModuleResponse;
import com.lms.learningpath.entity.StepModule;
import org.springframework.stereotype.Component;

@Component
public class StepModuleMapper {

    public StepModule toEntity(AddModuleToStepRequest request) {
        return StepModule.builder()
                .stepId(request.getStepId())
                .moduleType(request.getModuleType())
                .moduleOrder(request.getModuleOrder())
                .title(request.getTitle())
                .description(request.getDescription())
                .contentSetId(request.getContentSetId())
                .contentFolderId(request.getContentFolderId())
                .externalRefJson(request.getExternalRefJson())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .isActive(true)
                .build();
    }

    public StepModuleResponse toResponse(StepModule entity) {
        return StepModuleResponse.builder()
                .id(entity.getId())
                .stepId(entity.getStepId())
                .moduleType(entity.getModuleType())
                .moduleOrder(entity.getModuleOrder())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .contentSetId(entity.getContentSetId())
                .contentFolderId(entity.getContentFolderId())
                .externalRefJson(entity.getExternalRefJson())
                .isRequired(entity.getIsRequired())
                .isActive(entity.getIsActive())
                .createdDate(entity.getCreatedAt())
                .lastModifiedDate(entity.getUpdatedAt())
                .build();
    }
}
