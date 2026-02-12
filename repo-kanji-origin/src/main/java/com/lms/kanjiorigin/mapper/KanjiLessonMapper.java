package com.lms.kanjiorigin.mapper;

import com.lms.kanjiorigin.dto.request.CreateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.response.KanjiLessonBasicResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonResponse;
import com.lms.kanjiorigin.entity.KanjiLesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = { KanjiOriginMapper.class, KanjiLessonQuestionMapper.class })
public interface KanjiLessonMapper {

    KanjiLesson toEntity(CreateKanjiLessonRequest request);

    @Mapping(target = "studySetId", source = "studySet.id")
    @Mapping(target = "kanjiCount", expression = "java(lesson.getKanjiOrigins() == null ? 0 : lesson.getKanjiOrigins().size())")
    @Mapping(target = "questionCount", expression = "java(lesson.getQuestions() == null ? 0 : lesson.getQuestions().size())")
    @Mapping(target = "questions", source = "questions")
    KanjiLessonResponse toResponse(KanjiLesson lesson);

    @Mapping(target = "kanjiCount", expression = "java(lesson.getKanjiOrigins() == null ? 0 : lesson.getKanjiOrigins().size())")
    @Mapping(target = "questionCount", expression = "java(lesson.getQuestions() == null ? 0 : lesson.getQuestions().size())")
    KanjiLessonBasicResponse toBasicResponse(KanjiLesson lesson);

    List<KanjiLessonBasicResponse> toBasicResponseList(List<KanjiLesson> lessons);

    void updateEntity(@MappingTarget KanjiLesson lesson, UpdateKanjiLessonRequest request);
}
