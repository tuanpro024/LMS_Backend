package com.lms.content.common.mapper;

import com.lms.content.common.dto.request.CreateStudySetRequest;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.content.common.entity.StudySet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudySetMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "folders", ignore = true)
    StudySet toEntity(CreateStudySetRequest request);

    @Mapping(target = "totalItems", constant = "0")
    StudySetResponse toResponse(StudySet studySet);

    List<StudySetResponse> toResponseList(List<StudySet> studySets);
}
