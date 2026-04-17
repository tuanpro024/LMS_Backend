package com.lms.writing.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.writing.dto.request.CreateWordRequest;
import com.lms.writing.dto.request.UpdateWordRequest;
import com.lms.writing.dto.response.WordResponse;
import com.lms.writing.entity.Word;
import com.lms.writing.entity.UserWordProgress;
import com.lms.writing.event.WritingProgressUpdatedEvent;
import com.lms.writing.repository.UserWordProgressRepository;
import com.lms.content.common.entity.enums.ContentStatus;
import com.lms.writing.mapper.WordMapper;
import com.lms.writing.repository.WordRepository;
import com.lms.writing.service.WordService;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final WordMapper wordMapper;
    private final StudySetRepository studySetRepository;
    private final StudySetApiDelegate studySetApiDelegate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public WordResponse getWordById(String id) {
        Word word = wordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Word not found"));
        return wordMapper.toResponse(word);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WordResponse> getWordsByStudySetId(String studySetId) {
        List<Word> words = wordRepository.findByStudySetIdAndDeletedFalseOrderByIdAsc(studySetId);
        return wordMapper.toResponseList(words);
    }

    @Override
    public WordResponse updateWord(String id, UpdateWordRequest request, String userId) {
        Word word = wordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Word not found"));

        // Check ownership — only the study set owner or an admin can update
        if (!word.getStudySet().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this word");
        }

        // Update entity (auto-regenerates characters if word changed)
        wordMapper.updateEntity(word, request);

        Word updated = wordRepository.save(word);
        studySetApiDelegate.revertParentPackagesToDraft(updated.getStudySet().getId(), userId);
        return wordMapper.toResponse(updated);
    }

    @Override
    public void deleteWord(String id, String userId) {
        Word word = wordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Word not found"));

        // Check ownership — only the study set owner or an admin can delete
        if (!word.getStudySet().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this word");
        }

        String studySetId = word.getStudySet().getId();

        // Delete associated user progress records first to avoid FK constraint
        // violation
        userWordProgressRepository.deleteByWordId(id);

        wordRepository.delete(word);
        studySetApiDelegate.revertParentPackagesToDraft(studySetId, userId);
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
        List<Word> allWords = wordRepository.findByStudySetIdAndDeletedFalseOrderByIdAsc(studySetId);
        Set<String> learnedWordIds = new HashSet<>(
                userWordProgressRepository.findWordIdsByUserIdAndStudySetIdAndStatus(userId,
                        studySetId, ContentStatus.LEARNED));

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
        long total = wordRepository.countByStudySetIdAndDeletedFalse(studySetId);
        long learned = countLearnedWords(userId, studySetId);
        return total - learned;
    }

    @Override
    public void updateWordStatus(String userId, String wordId,
            com.lms.writing.dto.request.UpdateWordStatusRequest request) {
        Word word = wordRepository.findByIdAndDeletedFalse(wordId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Word not found"));

        String studySetId = word.getStudySet().getId();
        studySetApiDelegate.assertStudySetLearningAllowed(studySetId);
        Instant now = Instant.now();

        UserWordProgress progress = userWordProgressRepository.findByUserIdAndWordId(userId, wordId)
                .orElse(UserWordProgress.builder()
                        .userId(userId)
                        .word(word)
                        .status(ContentStatus.NOT_LEARNED)
                        .build());

        progress.setStatus(request.getStatus());
        progress.setLastReviewedAt(now);
        // Review count logic if needed
        if (request.getStatus() == ContentStatus.LEARNED) {
            progress.setReviewCount(progress.getReviewCount() + 1);
        }

        userWordProgressRepository.save(progress);

        long totalWords = wordRepository.countByStudySetIdAndDeletedFalse(studySetId);
        long learnedWords = userWordProgressRepository.countByUserIdAndStudySetIdAndStatus(userId, studySetId,
                ContentStatus.LEARNED);

        double progressPercentage = totalWords == 0
                ? 0.0
                : (learnedWords * 100.0) / totalWords;

        eventPublisher.publishEvent(WritingProgressUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .studySetId(studySetId)
                .learnedWords((int) learnedWords)
                .totalWords((int) totalWords)
                .progressPercentage(progressPercentage)
                .completed(totalWords > 0 && learnedWords >= totalWords)
                .occurredAt(now)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalWords(String studySetId) {
        return wordRepository.countByStudySetIdAndDeletedFalse(studySetId);
    }

    @Override
    public List<WordResponse> addWordsToStudySet(String studySetId, List<CreateWordRequest> words, String userId) {
        // Validate StudySet exists
        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        log.debug("Adding words to StudySet: studySetId={}, studySetOwnerId={}, requestUserId={}",
                studySetId, studySet.getUserId(), userId);

        // Check ownership — only the study set owner can add words
        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add words to this study set");
        }

        // Convert and save all words
        List<Word> wordEntities = words.stream()
                .map(request -> {
                    Word word = wordMapper.toEntity(request);
                    word.setStudySet(studySet);

                    // Handle characters JSON conversion if needed (similar to Card)
                    // The WordMapper should handle this automatically

                    return word;
                })
                .collect(Collectors.toList());

        List<Word> savedWords = wordRepository.saveAll(wordEntities);
        studySetApiDelegate.revertParentPackagesToDraft(studySetId, userId);

        log.info("Added {} words to StudySet {} by user {}", savedWords.size(), studySetId, userId);

        return wordMapper.toResponseList(savedWords);
    }
}
