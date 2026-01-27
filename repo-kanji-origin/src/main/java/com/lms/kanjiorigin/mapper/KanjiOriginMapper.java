package com.lms.kanjiorigin.mapper;

import com.lms.kanjiorigin.dto.request.CreateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.response.KanjiOriginResponse;
import com.lms.kanjiorigin.entity.KanjiOrigin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KanjiOriginMapper {

    @Mapping(target = "kanjiLesson", ignore = true)
    KanjiOrigin toEntity(CreateKanjiOriginRequest request);

    @Mapping(target = "kanjiLessonId", source = "kanjiLesson.id")
    KanjiOriginResponse toResponse(KanjiOrigin origin);

    List<KanjiOriginResponse> toResponseList(List<KanjiOrigin> origins);

    void updateEntity(@MappingTarget KanjiOrigin origin, UpdateKanjiOriginRequest request);
}
