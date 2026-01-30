package com.lms.learningpath.mapper;

import com.lms.learningpath.dto.request.CreateQuestRequest;
import com.lms.learningpath.dto.response.QuestResponse;
import com.lms.learningpath.entity.QuestDefinition;
import com.lms.learningpath.entity.UserQuestProgress;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @BeanMapping(builder = @Builder(disableBuilder = true))
    QuestDefinition toEntity(CreateQuestRequest request);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentValue", ignore = true)
    @Mapping(target = "targetValue", ignore = true)
    @Mapping(target = "period", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "claimedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    QuestResponse toResponse(QuestDefinition quest);

    List<QuestResponse> toResponseList(List<QuestDefinition> quests);

    default QuestResponse toResponseWithProgress(QuestDefinition quest, UserQuestProgress progress) {
        QuestResponse response = toResponse(quest);
        if (progress != null) {
            response.setStatus(progress.getStatus());
            response.setCurrentValue(progress.getCurrentValue());
            response.setTargetValue(progress.getTargetValue());
            response.setPeriod(progress.getPeriod());
            response.setCompletedAt(progress.getCompletedAt());
            response.setClaimedAt(progress.getClaimedAt());
            response.setExpiresAt(progress.getExpiresAt());
        }
        return response;
    }
}