package com.lms.pronunciation.service.impl;

import com.lms.common.dto.PageResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.pronunciation.dto.request.CreatePronunciationItemRequest;
import com.lms.pronunciation.dto.request.PronunciationItemSearchRequest;
import com.lms.pronunciation.dto.request.UpdatePronunciationItemRequest;
import com.lms.pronunciation.dto.response.PronunciationItemResponse;
import com.lms.pronunciation.entity.PronunciationItem;
import com.lms.pronunciation.mapper.PronunciationItemMapper;
import com.lms.pronunciation.repository.PronunciationItemRepository;
import com.lms.pronunciation.service.PronunciationItemService;
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
public class PronunciationItemServiceImpl implements PronunciationItemService {

    private final PronunciationItemRepository pronunciationItemRepository;
    private final StudySetRepository studySetRepository;
    private final PronunciationItemMapper pronunciationItemMapper;

    @Override
    public PronunciationItemResponse create(CreatePronunciationItemRequest request) {
        log.info("Creating pronunciation item with symbol: {}", request.getSymbol());

        StudySet studySet = studySetRepository.findById(request.getStudySetId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found with id: " + request.getStudySetId()));

        if (request.getContentIndex() != null &&
            pronunciationItemRepository.existsByStudySetIdAndContentIndexAndDeletedFalse(request.getStudySetId(), request.getContentIndex())) {
            throw new ApiException(ErrorCode.E227, "Content index already exists: " + request.getContentIndex());
        }

        if (pronunciationItemRepository.existsByStudySetIdAndSymbolAndDeletedFalse(request.getStudySetId(), request.getSymbol())) {
            throw new ApiException(ErrorCode.E227, "Symbol already exists in this study set: " + request.getSymbol());
        }

        PronunciationItem item = pronunciationItemMapper.toEntity(request);
        item.setStudySet(studySet);

        // Auto-assign contentIndex if not provided
        if (request.getContentIndex() == null) {
            Integer maxIndex = pronunciationItemRepository.findMaxContentIndexByStudySetId(request.getStudySetId());
            item.setContentIndex(maxIndex == null ? 1 : maxIndex + 1);
        }

        PronunciationItem savedItem = pronunciationItemRepository.save(item);
        return pronunciationItemMapper.toResponse(savedItem);
    }

    @Override
    public PronunciationItemResponse update(String id, UpdatePronunciationItemRequest request) {
        log.info("Updating pronunciation item with id: {}", id);

        PronunciationItem item = pronunciationItemRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "PronunciationItem not found with id: " + id));

        if (request.getContentIndex() != null &&
            !request.getContentIndex().equals(item.getContentIndex()) &&
            pronunciationItemRepository.existsByStudySetIdAndContentIndexAndIdNotAndDeletedFalse(
                item.getStudySet().getId(), request.getContentIndex(), id)) {
            throw new ApiException(ErrorCode.E227, "Content index already exists: " + request.getContentIndex());
        }

        pronunciationItemMapper.updateEntity(item, request);
        PronunciationItem savedItem = pronunciationItemRepository.save(item);
        return pronunciationItemMapper.toResponse(savedItem);
    }

    @Override
    public void delete(String id) {
        log.info("Deleting pronunciation item with id: {}", id);

        PronunciationItem item = pronunciationItemRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "PronunciationItem not found with id: " + id));

        item.setDeleted(true);
        pronunciationItemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public PronunciationItemResponse getById(String id) {
        log.info("Getting pronunciation item with id: {}", id);

        PronunciationItem item = pronunciationItemRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "PronunciationItem not found with id: " + id));

        return pronunciationItemMapper.toResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PronunciationItemResponse> getByStudySetId(String studySetId) {
        log.info("Getting pronunciation items for study set: {}", studySetId);

        if (!studySetRepository.existsById(studySetId)) {
            throw new ApiException(ErrorCode.E227, "StudySet not found with id: " + studySetId);
        }

        List<PronunciationItem> items = pronunciationItemRepository.findByStudySetIdAndDeletedFalse(studySetId);
        return pronunciationItemMapper.toResponseList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PronunciationItemResponse> search(PronunciationItemSearchRequest request) {
        log.info("Searching pronunciation items with studySetId: {}, type: {}, keyword: {}",
                request.getStudySetId(), request.getType(), request.getKeyword());

        List<PronunciationItem> items = pronunciationItemRepository.searchList(
                request.getStudySetId(), request.getType(), request.getKeyword());
        return pronunciationItemMapper.toResponseList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PronunciationItemResponse> searchPaged(PronunciationItemSearchRequest request) {
        log.info("Searching paged pronunciation items with studySetId: {}, type: {}, keyword: {}",
                request.getStudySetId(), request.getType(), request.getKeyword());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<PronunciationItem> page = pronunciationItemRepository.search(
                request.getStudySetId(), request.getType(), request.getKeyword(), pageable);

        List<PronunciationItemResponse> items = pronunciationItemMapper.toResponseList(page.getContent());

        return PageResponse.<PronunciationItemResponse>builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }
}
