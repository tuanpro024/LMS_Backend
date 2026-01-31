package com.lms.kanjiorigin.mapper;

import com.lms.kanjiorigin.dto.request.CreateQuestionRequest;
import com.lms.kanjiorigin.dto.request.UpdateQuestionRequest;
import com.lms.kanjiorigin.dto.response.QuestionResponse;
import com.lms.kanjiorigin.entity.KanjiLessonQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KanjiLessonQuestionMapper {

    KanjiLessonQuestion toEntity(CreateQuestionRequest request);

    @Mapping(target = "kanjiLessonId", source = "kanjiLesson.id")
    QuestionResponse toResponse(KanjiLessonQuestion question);

    List<QuestionResponse> toResponseList(List<KanjiLessonQuestion> questions);

    void updateEntity(@MappingTarget KanjiLessonQuestion question, UpdateQuestionRequest request);
}
