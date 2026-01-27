package com.lms.kanjiorigin.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.kanjiorigin.dto.request.CreateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.response.KanjiLessonBasicResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonResponse;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.mapper.KanjiLessonMapper;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.service.KanjiLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanjiLessonServiceImpl implements KanjiLessonService {

    private final KanjiLessonRepository kanjiLessonRepository;
    private final StudySetRepository studySetRepository;
    private final KanjiLessonMapper kanjiLessonMapper;

    @Override
    public KanjiLessonResponse createLesson(CreateKanjiLessonRequest request) {
        log.info("Creating kanji lesson with title: {}", request.getTitle());

        StudySet studySet = studySetRepository.findById(request.getStudySetId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found with id: " + request.getStudySetId()));

        KanjiLesson lesson = kanjiLessonMapper.toEntity(request);
        lesson.setStudySet(studySet);
        lesson.setContentIndex(0);

        KanjiLesson savedLesson = kanjiLessonRepository.save(lesson);
        return kanjiLessonMapper.toResponse(savedLesson);
    }

    @Override
    public KanjiLessonResponse updateLesson(String id, UpdateKanjiLessonRequest request) {
        log.info("Updating kanji lesson with id: {}", id);

        KanjiLesson lesson = kanjiLessonRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + id));

        kanjiLessonMapper.updateEntity(lesson, request);
        KanjiLesson savedLesson = kanjiLessonRepository.save(lesson);
        return kanjiLessonMapper.toResponse(savedLesson);
    }

    @Override
    public void deleteLesson(String id) {
        log.info("Deleting kanji lesson with id: {}", id);

        KanjiLesson lesson = kanjiLessonRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + id));

        if (!lesson.getKanjiOrigins().isEmpty()) {
            throw new ApiException(ErrorCode.E227, "Cannot delete lesson because it has kanji origins. Delete origins first.");
        }

        lesson.setDeleted(true);
        kanjiLessonRepository.save(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public KanjiLessonResponse getLesson(String id) {
        log.info("Getting kanji lesson with id: {}", id);

        KanjiLesson lesson = kanjiLessonRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + id));

        return kanjiLessonMapper.toResponse(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiLessonBasicResponse> getAllLessons() {
        log.info("Getting all kanji lessons");
        List<KanjiLesson> lessons = kanjiLessonRepository.findByDeletedFalse();
        return kanjiLessonMapper.toBasicResponseList(lessons);
    }
}
