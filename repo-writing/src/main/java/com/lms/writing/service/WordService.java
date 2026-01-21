package com.lms.writing.service;

import com.lms.writing.dto.request.UpdateWordRequest;
import com.lms.writing.dto.response.WordResponse;

import java.util.List;

public interface WordService {

    WordResponse getWordById(String id);

    List<WordResponse> getWordsByStudySetId(String studySetId);

    WordResponse updateWord(String id, UpdateWordRequest request, String userId);

    void deleteWord(String id, String userId);

    List<WordResponse> getLearnedWords(String studySetId);

    List<WordResponse> getNotLearnedWords(String studySetId);

    long countLearnedWords(String studySetId);

    long countNotLearnedWords(String studySetId);

    long countTotalWords(String studySetId);
}
