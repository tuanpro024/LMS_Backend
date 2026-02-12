package com.lms.kanjiorigin.service.impl;

import com.lms.common.dto.PageResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.kanjiorigin.dto.request.CreateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.request.KanjiOriginSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.response.KanjiOriginResponse;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.entity.KanjiOrigin;
import com.lms.kanjiorigin.mapper.KanjiOriginMapper;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.repository.KanjiOriginRepository;
import com.lms.kanjiorigin.service.KanjiOriginService;
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
public class KanjiOriginServiceImpl implements KanjiOriginService {

    private final KanjiOriginRepository kanjiOriginRepository;
    private final KanjiLessonRepository kanjiLessonRepository;
    private final KanjiOriginMapper kanjiOriginMapper;

    @Override
    public KanjiOriginResponse createOrigin(CreateKanjiOriginRequest request) {
        log.info("Creating kanji origin with term: {}", request.getTerm());

        KanjiLesson lesson = kanjiLessonRepository.findById(request.getKanjiLessonId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227,
                        "KanjiLesson not found with id: " + request.getKanjiLessonId()));

        Integer contentIndex = request.getContentIndex();
        if (contentIndex == null) {
            Integer maxContentIndex = kanjiOriginRepository
                    .findMaxContentIndexByKanjiLessonId(request.getKanjiLessonId());
            contentIndex = maxContentIndex == null ? 0 : maxContentIndex + 1;
        }

        if (kanjiOriginRepository.existsByKanjiLessonIdAndContentIndexAndDeletedFalse(request.getKanjiLessonId(),
                contentIndex)) {
            throw new ApiException(ErrorCode.E227,
                    "Content index already exists in this lesson: " + contentIndex);
        }

        if (kanjiOriginRepository.existsByTermAndKanjiLessonIdAndDeletedFalse(request.getTerm(),
                request.getKanjiLessonId())) {
            throw new ApiException(ErrorCode.E227, "Term already exists in this lesson: " + request.getTerm());
        }

        KanjiOrigin origin = kanjiOriginMapper.toEntity(request);
        origin.setKanjiLesson(lesson);
        origin.setContentIndex(contentIndex);

        KanjiOrigin savedOrigin = kanjiOriginRepository.save(origin);
        return kanjiOriginMapper.toResponse(savedOrigin);
    }

    @Override
    public KanjiOriginResponse updateOrigin(String id, UpdateKanjiOriginRequest request) {
        log.info("Updating kanji origin with id: {}", id);

        KanjiOrigin origin = kanjiOriginRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiOrigin not found with id: " + id));

        if (!origin.getTerm().equals(request.getTerm()) &&
                kanjiOriginRepository.existsByTermAndKanjiLessonIdAndDeletedFalse(request.getTerm(),
                        origin.getKanjiLesson().getId())) {
            throw new ApiException(ErrorCode.E227, "Term already exists in this lesson: " + request.getTerm());
        }

        if (request.getContentIndex() != null) {
            if (!request.getContentIndex().equals(origin.getContentIndex()) &&
                    kanjiOriginRepository.existsByKanjiLessonIdAndContentIndexAndIdNotAndDeletedFalse(
                            origin.getKanjiLesson().getId(), request.getContentIndex(), id)) {
                throw new ApiException(ErrorCode.E227,
                        "Content index already exists in this lesson: " + request.getContentIndex());
            }
        }

        kanjiOriginMapper.updateEntity(origin, request);
        KanjiOrigin savedOrigin = kanjiOriginRepository.save(origin);
        return kanjiOriginMapper.toResponse(savedOrigin);
    }

    @Override
    public void deleteOrigin(String id) {
        log.info("Deleting kanji origin with id: {}", id);

        KanjiOrigin origin = kanjiOriginRepository.findById(id).orElse(null);
        if (origin == null) {
            log.warn("Delete requested for non-existing kanji origin id: {}. Treating as no-op.", id);
            return;
        }
        if (origin.isDeleted()) {
            log.warn("Delete requested for already deleted kanji origin id: {}. Treating as no-op.", id);
            return;
        }

        origin.setDeleted(true);
        kanjiOriginRepository.save(origin);
    }

    @Override
    @Transactional(readOnly = true)
    public KanjiOriginResponse getOrigin(String id) {
        log.info("Getting kanji origin with id: {}", id);

        KanjiOrigin origin = kanjiOriginRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiOrigin not found with id: " + id));

        return kanjiOriginMapper.toResponse(origin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiOriginResponse> getOriginsByLesson(String lessonId) {
        log.info("Getting kanji origins for lesson: {}", lessonId);

        if (!kanjiLessonRepository.existsById(lessonId)) {
            throw new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + lessonId);
        }

        List<KanjiOrigin> origins = kanjiOriginRepository.findByKanjiLessonIdAndDeletedFalse(lessonId);
        return kanjiOriginMapper.toResponseList(origins);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiOriginResponse> search(KanjiOriginSearchRequest request) {
        log.info("Searching kanji origins with lessonId: {}, keyword: {}", request.getLessonId(), request.getKeyword());
        List<KanjiOrigin> origins = kanjiOriginRepository.searchList(request.getLessonId(), request.getKeyword());
        return kanjiOriginMapper.toResponseList(origins);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KanjiOriginResponse> searchPaged(KanjiOriginSearchRequest request) {
        log.info("Searching paged kanji origins with lessonId: {}, keyword: {}", request.getLessonId(),
                request.getKeyword());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<KanjiOrigin> page = kanjiOriginRepository.search(request.getLessonId(), request.getKeyword(), pageable);

        List<KanjiOriginResponse> items = kanjiOriginMapper.toResponseList(page.getContent());

        return PageResponse.<KanjiOriginResponse>builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }
}
