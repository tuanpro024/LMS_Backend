package com.lms.pronunciation.service;

import com.lms.pronunciation.dto.response.PronunciationItemProgressResponse;
import com.lms.pronunciation.dto.response.PronunciationStudySetProgressResponse;

import java.util.List;

public interface PronunciationProgressService {
    PronunciationItemProgressResponse markItemListened(String userId, String itemId);
    PronunciationStudySetProgressResponse getStudySetProgress(String userId, String studySetId);
    List<PronunciationItemProgressResponse> getItemProgressByStudySet(String userId, String studySetId);
    List<PronunciationStudySetProgressResponse> getUserStudySetHistory(String userId);
    void updateStudySetProgress(String userId, String studySetId);
}
