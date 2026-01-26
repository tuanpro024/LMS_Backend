package com.lms.writing.service;

import com.lms.writing.dto.request.UpdateWordRequest;
import com.lms.writing.dto.response.WordResponse;

import java.util.List;

public interface WordService {

    WordResponse getWordById(String id);

    List<WordResponse> getWordsByStudySetId(String studySetId);

    WordResponse updateWord(String id, UpdateWordRequest request, String userId);

    void deleteWord(String id, String userId);

    List<WordResponse> getLearnedWords(String userId, String studySetId);

    List<WordResponse> getNotLearnedWords(String userId, String studySetId);

    long countLearnedWords(String userId, String studySetId);

    long countNotLearnedWords(String userId, String studySetId);

    void updateWordStatus(String userId, String wordId, com.lms.writing.dto.request.UpdateWordStatusRequest request);

    long countTotalWords(String studySetId);
}
