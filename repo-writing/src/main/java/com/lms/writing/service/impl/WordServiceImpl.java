package com.lms.writing.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.writing.dto.request.UpdateWordRequest;
import com.lms.writing.dto.response.WordResponse;
import com.lms.writing.entity.Word;
import com.lms.writing.entity.enums.WordStatus;
import com.lms.writing.entity.UserWordProgress;
import com.lms.writing.repository.UserWordProgressRepository;
import com.lms.content.common.entity.enums.ContentStatus;
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
    private final UserWordProgressRepository userWordProgressRepository;
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
    public List<WordResponse> getLearnedWords(String userId, String studySetId) {
        List<String> wordIds = userWordProgressRepository.findWordIdsByUserIdAndStudySetIdAndStatus(userId, studySetId,
                ContentStatus.LEARNED);
        List<Word> words = wordRepository.findAllById(wordIds);
        List<WordResponse> responses = wordMapper.toResponseList(words);
        responses.forEach(r -> r.setStatus(com.lms.writing.entity.enums.WordStatus.LEARNED));
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WordResponse> getNotLearnedWords(String userId, String studySetId) {
        List<Word> allWords = wordRepository.findByStudySetIdOrderByIdAsc(studySetId);
        List<String> learnedWordIds = userWordProgressRepository.findWordIdsByUserIdAndStudySetIdAndStatus(userId,
                studySetId, ContentStatus.LEARNED);

        List<Word> notLearnedWords = allWords.stream()
                .filter(w -> !learnedWordIds.contains(w.getId()))
                .toList();

        List<WordResponse> responses = wordMapper.toResponseList(notLearnedWords);
        responses.forEach(r -> r.setStatus(com.lms.writing.entity.enums.WordStatus.NOT_LEARNED));
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public long countLearnedWords(String userId, String studySetId) {
        return userWordProgressRepository.countByUserIdAndStudySetIdAndStatus(userId, studySetId,
                ContentStatus.LEARNED);
    }

    @Override
    @Transactional(readOnly = true)
    public long countNotLearnedWords(String userId, String studySetId) {
        long total = wordRepository.countByStudySetId(studySetId);
        long learned = countLearnedWords(userId, studySetId);
        return total - learned;
    }

    @Override
    public void updateWordStatus(String userId, String wordId,
            com.lms.writing.dto.request.UpdateWordStatusRequest request) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Word not found"));

        UserWordProgress progress = userWordProgressRepository.findByUserIdAndWordId(userId, wordId)
                .orElse(UserWordProgress.builder()
                        .userId(userId)
                        .word(word)
                        .status(ContentStatus.NOT_LEARNED)
                        .build());

        progress.setStatus(request.getStatus());
        progress.setLastReviewedAt(java.time.Instant.now());
        // Review count logic if needed
        if (request.getStatus() == ContentStatus.LEARNED) {
            progress.setReviewCount(progress.getReviewCount() + 1);
        }

        userWordProgressRepository.save(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalWords(String studySetId) {
        List<Word> words = wordRepository.findByStudySetId(studySetId);
        return words.size();
    }
}
