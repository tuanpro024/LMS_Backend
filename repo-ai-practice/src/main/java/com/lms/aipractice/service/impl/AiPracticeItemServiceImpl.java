package com.lms.aipractice.service.impl;

import com.lms.aipractice.dto.request.CreateAiPracticeItemRequest;
import com.lms.aipractice.dto.request.UpdateAiPracticeItemRequest;
import com.lms.aipractice.dto.response.AiPracticeItemResponse;
import com.lms.aipractice.entity.AiPracticeItem;
import com.lms.aipractice.mapper.AiPracticeMapper;
import com.lms.aipractice.repository.AiPracticeItemRepository;
import com.lms.aipractice.service.AiPracticeItemService;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AiPracticeItemServiceImpl implements AiPracticeItemService {

    private final AiPracticeItemRepository itemRepository;
    private final StudySetRepository studySetRepository;
    private final AiPracticeMapper mapper;

    @Override
    public AiPracticeItemResponse create(CreateAiPracticeItemRequest request) {
        log.info("Creating AI practice item subtype={} in studySet={}", request.getQuestionSubtype(), request.getStudySetId());

        StudySet studySet = studySetRepository.findById(request.getStudySetId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found: " + request.getStudySetId()));

        if (request.getContentIndex() != null &&
                itemRepository.existsByStudySetIdAndContentIndexAndDeletedFalse(
                        request.getStudySetId(), request.getContentIndex())) {
            throw new ApiException(ErrorCode.E227, "Content index already exists: " + request.getContentIndex());
        }

        AiPracticeItem item = mapper.toEntity(request);
        item.setStudySet(studySet);

        // Auto-assign contentIndex
        if (request.getContentIndex() == null) {
            Integer maxIndex = itemRepository.findMaxContentIndexByStudySetId(request.getStudySetId());
            item.setContentIndex(maxIndex == null ? 1 : maxIndex + 1);
        } else {
            item.setContentIndex(request.getContentIndex());
        }

        return mapper.toResponse(itemRepository.save(item));
    }

    @Override
    public AiPracticeItemResponse update(String id, UpdateAiPracticeItemRequest request) {
        log.info("Updating AI practice item id={}", id);

        AiPracticeItem item = itemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "AiPracticeItem not found: " + id));

        if (request.getContentIndex() != null &&
                !request.getContentIndex().equals(item.getContentIndex()) &&
                itemRepository.existsByStudySetIdAndContentIndexAndIdNotAndDeletedFalse(
                        item.getStudySet().getId(), request.getContentIndex(), id)) {
            throw new ApiException(ErrorCode.E227, "Content index already exists: " + request.getContentIndex());
        }

        mapper.updateEntity(item, request);
        return mapper.toResponse(itemRepository.save(item));
    }

    @Override
    public void delete(String id) {
        log.info("Deleting AI practice item id={}", id);
        AiPracticeItem item = itemRepository.findById(id).orElse(null);
        if (item == null || item.isDeleted()) {
            log.warn("Delete no-op for item id={}", id);
            return;
        }
        item.setDeleted(true);
        itemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public AiPracticeItemResponse getById(String id) {
        return mapper.toResponse(
                itemRepository.findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ApiException(ErrorCode.E227, "AiPracticeItem not found: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiPracticeItemResponse> getByStudySetId(String studySetId) {
        if (!studySetRepository.existsById(studySetId)) {
            throw new ApiException(ErrorCode.E227, "StudySet not found: " + studySetId);
        }
        return mapper.toResponseList(
                itemRepository.findByStudySetIdAndDeletedFalseOrderByContentIndexAsc(studySetId));
    }
}
