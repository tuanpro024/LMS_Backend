package com.lms.flashcard.service;

import com.lms.flashcard.dto.response.FlashcardStudySetProgressResponse;

import java.util.List;

public interface FlashcardProgressService {
    FlashcardStudySetProgressResponse updateStudySetProgress(String userId, String studySetId);
    FlashcardStudySetProgressResponse getStudySetProgress(String userId, String studySetId);
    List<FlashcardStudySetProgressResponse> getUserStudySetHistory(String userId);
}
