package com.lms.writing.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.writing.dto.request.UpdateWordRequest;
import com.lms.writing.dto.response.WordResponse;
import com.lms.writing.entity.Word;
import com.lms.writing.entity.enums.WordStatus;
import com.lms.writing.mapper.WordMapper;
import com.lms.writing.repository.WordRepository;
import com.lms.writing.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final WordMapper wordMapper;

    @Override
    @Transactional(readOnly = true)
    public WordResponse getWordById(String id) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Word not found"));
        return wordMapper.toResponse(word);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WordResponse> getWordsByStudySetId(String studySetId) {
        List<Word> words = wordRepository.findByStudySetIdOrderByIdAsc(studySetId);
        return wordMapper.toResponseList(words);
    }

    @Override
    public WordResponse updateWord(String id, UpdateWordRequest request, String userId) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Word not found"));

        // Check ownership via studySet
        if (!word.getStudySet().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this word");
        }

        // Update entity (auto-regenerates characters if word changed)
        wordMapper.updateEntity(word, request);

        Word updated = wordRepository.save(word);
        return wordMapper.toResponse(updated);
    }

    @Override
    public void deleteWord(String id, String userId) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Word not found"));

        // Check ownership
        if (!word.getStudySet().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this word");
        }

        wordRepository.delete(word);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WordResponse> getLearnedWords(String studySetId) {
        List<Word> words = wordRepository.findByStudySetIdAndStatus(studySetId, WordStatus.LEARNED);
        return wordMapper.toResponseList(words);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WordResponse> getNotLearnedWords(String studySetId) {
        List<Word> words = wordRepository.findByStudySetIdAndStatus(studySetId, WordStatus.NOT_LEARNED);
        return wordMapper.toResponseList(words);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLearnedWords(String studySetId) {
        return wordRepository.countByStudySetIdAndStatus(studySetId, WordStatus.LEARNED);
    }

    @Override
    @Transactional(readOnly = true)
    public long countNotLearnedWords(String studySetId) {
        return wordRepository.countByStudySetIdAndStatus(studySetId, WordStatus.NOT_LEARNED);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalWords(String studySetId) {
        List<Word> words = wordRepository.findByStudySetId(studySetId);
        return words.size();
    }
}
