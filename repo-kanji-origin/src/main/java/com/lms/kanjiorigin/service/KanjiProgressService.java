package com.lms.kanjiorigin.service;

import com.lms.kanjiorigin.dto.request.UpdateKanjiStatusRequest;
import com.lms.kanjiorigin.dto.response.KanjiStatusResponse;
import com.lms.kanjiorigin.dto.response.KanjiStudySetProgressResponse;

import java.util.List;

public interface KanjiProgressService {

    KanjiStatusResponse updateKanjiStatus(String userId, String kanjiId, UpdateKanjiStatusRequest request);

    KanjiStudySetProgressResponse getStudySetProgress(String userId, String studySetId);

    List<KanjiStatusResponse> getStudySetKanjiStatuses(String userId, String studySetId);

    List<KanjiStudySetProgressResponse> getUserStudySetHistory(String userId);
}