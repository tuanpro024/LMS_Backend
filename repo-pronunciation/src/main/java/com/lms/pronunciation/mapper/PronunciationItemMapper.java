package com.lms.pronunciation.mapper;

import com.lms.pronunciation.dto.request.CreatePronunciationItemRequest;
import com.lms.pronunciation.dto.request.UpdatePronunciationItemRequest;
import com.lms.pronunciation.dto.response.PronunciationItemResponse;
import com.lms.pronunciation.entity.PronunciationItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface PronunciationItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "studySet", ignore = true)
    PronunciationItem toEntity(CreatePronunciationItemRequest request);

    @Mapping(target = "studySetId", source = "studySet.id")
    PronunciationItemResponse toResponse(PronunciationItem item);

    List<PronunciationItemResponse> toResponseList(List<PronunciationItem> items);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "studySet", ignore = true)
    void updateEntity(@MappingTarget PronunciationItem item, UpdatePronunciationItemRequest request);
}
