package com.lms.flashcard.mapper;

import com.lms.flashcard.dto.request.CreateStudySetRequest;
import com.lms.flashcard.dto.response.StudySetResponse;
import com.lms.flashcard.entity.StudySet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { CardMapper.class })
public interface StudySetMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "folders", ignore = true)
    StudySet toEntity(CreateStudySetRequest request);

    StudySetResponse toResponse(StudySet studySet);

    List<StudySetResponse> toResponseList(List<StudySet> studySets);
}
