package com.lms.kanjiorigin.service.impl;

import com.lms.common.dto.PageResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.kanjiorigin.dto.request.CreateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.request.KanjiOriginSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.response.KanjiOriginResponse;
import com.lms.kanjiorigin.entity.KanjiOrigin;
import com.lms.kanjiorigin.mapper.KanjiOriginMapper;
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
    private final StudySetRepository studySetRepository;
    private final KanjiOriginMapper kanjiOriginMapper;

    @Override
    public KanjiOriginResponse createOrigin(CreateKanjiOriginRequest request) {
        log.info("Creating kanji origin with term: {}", request.getTerm());

        StudySet studySet = studySetRepository.findById(request.getStudySetId())
            .orElseThrow(
                () -> new ApiException(ErrorCode.E227, "StudySet not found with id: " + request.getStudySetId()));

        Integer contentIndex = request.getContentIndex();
        if (contentIndex == null) {
            Integer maxContentIndex = kanjiOriginRepository
                .findMaxContentIndexByStudySetId(request.getStudySetId());
            contentIndex = maxContentIndex == null ? 0 : maxContentIndex + 1;
        }

        if (kanjiOriginRepository.existsByStudySetIdAndContentIndexAndDeletedFalse(request.getStudySetId(),
                contentIndex)) {
            throw new ApiException(ErrorCode.E227,
                "Content index already exists in this study set: " + contentIndex);
        }

        if (kanjiOriginRepository.existsByTermAndStudySetIdAndDeletedFalse(request.getTerm(),
            request.getStudySetId())) {
            throw new ApiException(ErrorCode.E227, "Term already exists in this study set: " + request.getTerm());
        }

        KanjiOrigin origin = kanjiOriginMapper.toEntity(request);
        origin.setStudySet(studySet);
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
            kanjiOriginRepository.existsByTermAndStudySetIdAndDeletedFalse(request.getTerm(),
                origin.getStudySet().getId())) {
            throw new ApiException(ErrorCode.E227, "Term already exists in this study set: " + request.getTerm());
        }

        if (request.getContentIndex() != null) {
            if (!request.getContentIndex().equals(origin.getContentIndex()) &&
                kanjiOriginRepository.existsByStudySetIdAndContentIndexAndIdNotAndDeletedFalse(
                    origin.getStudySet().getId(), request.getContentIndex(), id)) {
                throw new ApiException(ErrorCode.E227,
                "Content index already exists in this study set: " + request.getContentIndex());
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
    public List<KanjiOriginResponse> getOriginsByStudySet(String studySetId) {
        log.info("Getting kanji origins for study set: {}", studySetId);
        List<KanjiOrigin> origins = kanjiOriginRepository.findByStudySetIdAndDeletedFalseOrderByContentIndexAsc(studySetId);
        return kanjiOriginMapper.toResponseList(origins);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiOriginResponse> search(KanjiOriginSearchRequest request) {
        log.info("Searching kanji origins with studySetId: {}, keyword: {}", request.getStudySetId(),
                request.getKeyword());
        List<KanjiOrigin> origins = kanjiOriginRepository.searchList(request.getStudySetId(), request.getKeyword());
        return kanjiOriginMapper.toResponseList(origins);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KanjiOriginResponse> searchPaged(KanjiOriginSearchRequest request) {
        log.info("Searching paged kanji origins with studySetId: {}, keyword: {}", request.getStudySetId(),
                request.getKeyword());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<KanjiOrigin> page = kanjiOriginRepository.search(request.getStudySetId(), request.getKeyword(), pageable);

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
