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

    @Mapping(target = "kanjiLesson", ignore = true)
    @Mapping(target = "kanjiQuestion", ignore = true)
    @Mapping(target = "contentIndex", ignore = true)
    KanjiLessonQuestion toEntity(CreateQuestionRequest request);

    @Mapping(target = "id", source = "kanjiQuestion.id")
    @Mapping(target = "kanjiLessonId", source = "kanjiLesson.id")
    @Mapping(target = "contentIndex", source = "contentIndex")
    @Mapping(target = "content", source = "kanjiQuestion.content")
    @Mapping(target = "correctAnswer", source = "kanjiQuestion.correctAnswer")
    @Mapping(target = "wrongOptions", expression = "java(question.getKanjiQuestion().getWrongOptions().stream().map(com.lms.kanjiorigin.entity.KanjiQuestionWrongOption::getWrongOption).toList())")
    QuestionResponse toResponse(KanjiLessonQuestion question);

    List<QuestionResponse> toResponseList(List<KanjiLessonQuestion> questions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "kanjiLesson", ignore = true)
    @Mapping(target = "kanjiQuestion", ignore = true)
    @Mapping(target = "contentIndex", ignore = true)
    void updateEntity(@MappingTarget KanjiLessonQuestion question, UpdateQuestionRequest request);
}
