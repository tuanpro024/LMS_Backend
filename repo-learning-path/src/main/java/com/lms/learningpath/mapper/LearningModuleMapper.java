package com.lms.learningpath.mapper;

import com.lms.learningpath.dto.request.CreateModuleRequest;
import com.lms.learningpath.dto.response.ModuleResponse;
import com.lms.learningpath.entity.LearningModule;
import com.lms.learningpath.entity.UserModuleProgress;
import com.lms.learningpath.entity.enums.ModuleStatus;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearningModuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @BeanMapping(builder = @Builder(disableBuilder = true))
    LearningModule toEntity(CreateModuleRequest request);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "attempts", ignore = true)
    @Mapping(target = "lastAttemptAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    ModuleResponse toResponse(LearningModule module);

    List<ModuleResponse> toResponseList(List<LearningModule> modules);

    default ModuleResponse toResponseWithProgress(LearningModule module, UserModuleProgress progress) {
        ModuleResponse response = toResponse(module);
        if (progress != null) {
            response.setStatus(progress.getStatus());
            response.setScore(progress.getScore());
            response.setAttempts(progress.getAttempts());
            response.setLastAttemptAt(progress.getLastAttemptAt());
            response.setCompletedAt(progress.getCompletedAt());
        } else {
            response.setStatus(ModuleStatus.NOT_STARTED);
        }
        return response;
    }
}