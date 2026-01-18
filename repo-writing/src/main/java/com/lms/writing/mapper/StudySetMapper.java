package com.lms.writing.mapper;

import com.lms.writing.dto.request.CreateStudySetRequest;
import com.lms.writing.dto.request.UpdateStudySetRequest;
import com.lms.writing.dto.response.StudySetResponse;
import com.lms.writing.entity.StudySet;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudySetMapper {

    @Mapping(target = "words", ignore = true)
    @Mapping(target = "folders", ignore = true)
    StudySet toEntity(CreateStudySetRequest request);

    @Mapping(target = "words", ignore = true)
    StudySetResponse toResponse(StudySet studySet);

    List<StudySetResponse> toResponseList(List<StudySet> studySets);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "words", ignore = true)
    @Mapping(target = "folders", ignore = true)
    void updateEntity(@MappingTarget StudySet studySet, UpdateStudySetRequest request);
}
