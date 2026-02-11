package com.lms.pronunciation.service;

import com.lms.common.dto.PageResponse;
import com.lms.pronunciation.dto.request.CreatePronunciationItemRequest;
import com.lms.pronunciation.dto.request.PronunciationItemSearchRequest;
import com.lms.pronunciation.dto.request.UpdatePronunciationItemRequest;
import com.lms.pronunciation.dto.response.PronunciationItemResponse;

import java.util.List;

public interface PronunciationItemService {

    PronunciationItemResponse create(CreatePronunciationItemRequest request);

    PronunciationItemResponse update(String id, UpdatePronunciationItemRequest request);

    void delete(String id);

    PronunciationItemResponse getById(String id);

    List<PronunciationItemResponse> getByStudySetId(String studySetId);

    List<PronunciationItemResponse> search(PronunciationItemSearchRequest request);

    PageResponse<PronunciationItemResponse> searchPaged(PronunciationItemSearchRequest request);
}
