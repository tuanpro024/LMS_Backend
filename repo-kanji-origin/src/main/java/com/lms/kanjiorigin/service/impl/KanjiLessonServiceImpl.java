package com.lms.kanjiorigin.service.impl;

import com.lms.common.dto.PageResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.kanjiorigin.dto.request.CreateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.request.KanjiLessonSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.response.KanjiLessonBasicResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonResponse;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.mapper.KanjiLessonMapper;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.service.KanjiLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        if (request.getContentIndex() != null) {
            boolean exists = kanjiLessonRepository.existsByStudySetIdAndContentIndex(request.getStudySetId(), request.getContentIndex());
            if (exists) {
                throw new ApiException(ErrorCode.E227, "Content index " + request.getContentIndex() + " already exists in this StudySet");
            }
        }

        if (kanjiLessonRepository.existsByStudySetIdAndTitle(request.getStudySetId(), request.getTitle())) {
            throw new ApiException(ErrorCode.E227, "Title '" + request.getTitle() + "' already exists in this StudySet");
        }

        KanjiLesson lesson = kanjiLessonMapper.toEntity(request);
        lesson.setStudySet(studySet);
        if (request.getContentIndex() == null) {
             throw new ApiException(ErrorCode.E227, "Content index is required");
        }
        
        KanjiLesson savedLesson = kanjiLessonRepository.save(lesson);
        return kanjiLessonMapper.toResponse(savedLesson);
    }

    @Override
    public KanjiLessonResponse updateLesson(String id, UpdateKanjiLessonRequest request) {
        log.info("Updating kanji lesson with id: {}", id);

        KanjiLesson lesson = kanjiLessonRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + id));

        kanjiLessonMapper.updateEntity(lesson, request);
        
        if (request.getContentIndex() != null) {
             boolean exists = kanjiLessonRepository.existsByStudySetIdAndContentIndexAndIdNot(
                 lesson.getStudySet().getId(), 
                 request.getContentIndex(), 
                 id
             );
             if (exists) {
                 throw new ApiException(ErrorCode.E227, "Content index " + request.getContentIndex() + " already exists in this StudySet");
             }
        }

        if (request.getTitle() != null && !request.getTitle().equals(lesson.getTitle())) {
             if (kanjiLessonRepository.existsByStudySetIdAndTitleAndIdNot(
                 lesson.getStudySet().getId(), 
                 request.getTitle(), 
                 id
             )) {
                 throw new ApiException(ErrorCode.E227, "Title '" + request.getTitle() + "' already exists in this StudySet");
             }
        }
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

    @Override
    @Transactional(readOnly = true)
    public List<KanjiLessonBasicResponse> getLessonsByStudySetId(String studySetId) {
        log.info("Getting kanji lessons by study set id: {}", studySetId);
        List<KanjiLesson> lessons = kanjiLessonRepository.findByStudySetIdAndDeletedFalse(studySetId);
        return kanjiLessonMapper.toBasicResponseList(lessons);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiLessonBasicResponse> search(KanjiLessonSearchRequest request) {
        log.info("Searching kanji lessons with studySetId: {}, keyword: {}", request.getStudySetId(), request.getKeyword());
        List<KanjiLesson> lessons = kanjiLessonRepository.searchList(request.getStudySetId(), request.getKeyword());
        return kanjiLessonMapper.toBasicResponseList(lessons);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KanjiLessonBasicResponse> searchPaged(KanjiLessonSearchRequest request) {
        log.info("Searching paged kanji lessons with studySetId: {}, keyword: {}", request.getStudySetId(), request.getKeyword());
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<KanjiLesson> page = kanjiLessonRepository.search(request.getStudySetId(), request.getKeyword(), pageable);
        
        List<KanjiLessonBasicResponse> items = kanjiLessonMapper.toBasicResponseList(page.getContent());
        
        return PageResponse.<KanjiLessonBasicResponse>builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }
}

