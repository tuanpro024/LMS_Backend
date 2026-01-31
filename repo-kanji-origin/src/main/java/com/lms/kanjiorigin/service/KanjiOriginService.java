package com.lms.kanjiorigin.service;

import com.lms.kanjiorigin.dto.request.CreateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.response.KanjiOriginResponse;

import java.util.List;

public interface KanjiOriginService {
    KanjiOriginResponse createOrigin(CreateKanjiOriginRequest request);
    KanjiOriginResponse updateOrigin(String id, UpdateKanjiOriginRequest request);
    void deleteOrigin(String id);
    KanjiOriginResponse getOrigin(String id);
    List<KanjiOriginResponse> getOriginsByLesson(String lessonId);
}
