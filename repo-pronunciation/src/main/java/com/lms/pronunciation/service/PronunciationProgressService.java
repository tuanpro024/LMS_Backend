package com.lms.pronunciation.service;

import com.lms.pronunciation.dto.response.PronunciationItemProgressResponse;
import com.lms.pronunciation.dto.response.PronunciationStudySetProgressResponse;

public interface PronunciationProgressService {
    PronunciationItemProgressResponse markItemListened(String userId, String itemId);
    PronunciationStudySetProgressResponse getStudySetProgress(String userId, String studySetId);
    java.util.List<PronunciationItemProgressResponse> getItemProgressByStudySet(String userId, String studySetId);
    void updateStudySetProgress(String userId, String studySetId);
}
