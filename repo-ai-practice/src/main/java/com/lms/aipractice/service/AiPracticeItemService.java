package com.lms.aipractice.service;

import com.lms.aipractice.dto.request.CreateAiPracticeItemRequest;
import com.lms.aipractice.dto.request.UpdateAiPracticeItemRequest;
import com.lms.aipractice.dto.response.AiPracticeItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AiPracticeItemService {
    AiPracticeItemResponse create(CreateAiPracticeItemRequest request);

    AiPracticeItemResponse createWithAudio(CreateAiPracticeItemRequest request, MultipartFile audioFile);

    AiPracticeItemResponse createWithImage(CreateAiPracticeItemRequest request, MultipartFile imageFile);

    AiPracticeItemResponse update(String id, UpdateAiPracticeItemRequest request);

    void delete(String id);

    AiPracticeItemResponse getById(String id);

    List<AiPracticeItemResponse> getByStudySetId(String studySetId);
}
