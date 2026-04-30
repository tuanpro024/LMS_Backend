package com.lms.kanjiorigin.mapper;

import com.lms.kanjiorigin.dto.request.CreateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.response.KanjiOriginResponse;
import com.lms.kanjiorigin.entity.KanjiOrigin;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface KanjiOriginMapper {

    @Mapping(target = "studySet", ignore = true)
    KanjiOrigin toEntity(CreateKanjiOriginRequest request);

    @Mapping(target = "studySetId", source = "studySet.id")
    KanjiOriginResponse toResponse(KanjiOrigin origin);

    List<KanjiOriginResponse> toResponseList(List<KanjiOrigin> origins);

    void updateEntity(@MappingTarget KanjiOrigin origin, UpdateKanjiOriginRequest request);
}
