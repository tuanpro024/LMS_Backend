package com.lms.aipractice.mapper;

import com.lms.aipractice.dto.request.CreateAiPracticeItemRequest;
import com.lms.aipractice.dto.request.UpdateAiPracticeItemRequest;
import com.lms.aipractice.dto.response.AiPracticeItemResponse;
import com.lms.aipractice.dto.response.AttemptResponse;
import com.lms.aipractice.dto.response.GradingJobResponse;
import com.lms.aipractice.entity.AiGradingJob;
import com.lms.aipractice.entity.AiPracticeAttempt;
import com.lms.aipractice.entity.AiPracticeItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiPracticeMapper {

    @Mapping(target = "studySet", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "contentIndex", ignore = true)
    AiPracticeItem toEntity(CreateAiPracticeItemRequest request);

    @Mapping(target = "studySetId", source = "studySet.id")
    AiPracticeItemResponse toResponse(AiPracticeItem item);

    List<AiPracticeItemResponse> toResponseList(List<AiPracticeItem> items);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "studySet", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(@MappingTarget AiPracticeItem item, UpdateAiPracticeItemRequest request);

    AttemptResponse toAttemptResponse(AiPracticeAttempt attempt);

    @Mapping(target = "createdAt", source = "createdAt")
    GradingJobResponse toJobResponse(AiGradingJob job);
}
