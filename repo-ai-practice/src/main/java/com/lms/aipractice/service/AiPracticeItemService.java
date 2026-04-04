package com.lms.aipractice.service;

import com.lms.aipractice.dto.request.CreateAiPracticeItemRequest;
import com.lms.aipractice.dto.request.UpdateAiPracticeItemRequest;
import com.lms.aipractice.dto.response.AiPracticeItemResponse;

import java.util.List;

public interface AiPracticeItemService {
    AiPracticeItemResponse create(CreateAiPracticeItemRequest request);
    AiPracticeItemResponse update(String id, UpdateAiPracticeItemRequest request);
    void delete(String id);
    AiPracticeItemResponse getById(String id);
    List<AiPracticeItemResponse> getByStudySetId(String studySetId);
}
