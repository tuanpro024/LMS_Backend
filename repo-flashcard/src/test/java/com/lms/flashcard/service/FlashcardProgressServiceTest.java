package com.lms.flashcard.service;

import com.lms.flashcard.dto.response.FlashcardStudySetProgressResponse;
import com.lms.flashcard.entity.FlashcardStudySetProgress;
import com.lms.flashcard.entity.enums.CardStatus;
import com.lms.flashcard.entity.enums.ProgressStatus;
import com.lms.flashcard.repository.CardRepository;
import com.lms.flashcard.repository.FlashcardStudySetProgressRepository;
import com.lms.flashcard.repository.UserCardProgressRepository;
import com.lms.flashcard.service.impl.FlashcardProgressServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardProgressServiceTest {

    @Mock
    private FlashcardStudySetProgressRepository progressRepository;

    @Mock
    private UserCardProgressRepository cardProgressRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private FlashcardProgressServiceImpl progressService;

    private final String userId = "user-123";
    private final String studySetId = "set-456";

    @Test
    void updateStudySetProgress_NoCards_ShouldBeNotStarted() {
        when(cardRepository.countByStudySetIdAndDeletedFalse(studySetId)).thenReturn(0L);
        when(cardProgressRepository.countByUserIdAndStudySetIdAndStatus(userId, studySetId, CardStatus.LEARNED)).thenReturn(0L);
        when(progressRepository.findByUserIdAndStudySetId(userId, studySetId)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FlashcardStudySetProgressResponse response = progressService.updateStudySetProgress(userId, studySetId);

        assertEquals(ProgressStatus.NOT_STARTED, response.getStatus());
        assertEquals(0, response.getLearnedCards());
        assertEquals(0, response.getTotalCards());
        assertEquals(0.0, response.getProgressPercentage());
    }

    @Test
    void updateStudySetProgress_SomeCardsLearned_ShouldBeInProgress() {
        when(cardRepository.countByStudySetIdAndDeletedFalse(studySetId)).thenReturn(10L);
        when(cardProgressRepository.countByUserIdAndStudySetIdAndStatus(userId, studySetId, CardStatus.LEARNED)).thenReturn(5L);
        when(progressRepository.findByUserIdAndStudySetId(userId, studySetId)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FlashcardStudySetProgressResponse response = progressService.updateStudySetProgress(userId, studySetId);

        assertEquals(ProgressStatus.IN_PROGRESS, response.getStatus());
        assertEquals(5, response.getLearnedCards());
        assertEquals(10, response.getTotalCards());
        assertEquals(0.5, response.getProgressPercentage());
    }

    @Test
    void updateStudySetProgress_AllCardsLearned_ShouldBeCompleted() {
        when(cardRepository.countByStudySetIdAndDeletedFalse(studySetId)).thenReturn(10L);
        when(cardProgressRepository.countByUserIdAndStudySetIdAndStatus(userId, studySetId, CardStatus.LEARNED)).thenReturn(10L);
        when(progressRepository.findByUserIdAndStudySetId(userId, studySetId)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FlashcardStudySetProgressResponse response = progressService.updateStudySetProgress(userId, studySetId);

        assertEquals(ProgressStatus.COMPLETED, response.getStatus());
        assertEquals(10, response.getLearnedCards());
        assertEquals(1, response.getProgressPercentage());
        assertNotNull(response.getCompletedAt());
    }

    @Test
    void updateStudySetProgress_FromCompletedToInProgress_ShouldResetCompletedAt() {
        FlashcardStudySetProgress existingProgress = FlashcardStudySetProgress.builder()
                .userId(userId)
                .studySetId(studySetId)
                .status(ProgressStatus.COMPLETED)
                .learnedCards(10)
                .totalCards(10)
                .progressPercentage(1.0)
                .completedAt(java.time.Instant.now())
                .build();

        when(cardRepository.countByStudySetIdAndDeletedFalse(studySetId)).thenReturn(10L);
        when(cardProgressRepository.countByUserIdAndStudySetIdAndStatus(userId, studySetId, CardStatus.LEARNED)).thenReturn(9L);
        when(progressRepository.findByUserIdAndStudySetId(userId, studySetId)).thenReturn(Optional.of(existingProgress));
        when(progressRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FlashcardStudySetProgressResponse response = progressService.updateStudySetProgress(userId, studySetId);

        assertEquals(ProgressStatus.IN_PROGRESS, response.getStatus());
        assertEquals(9, response.getLearnedCards());
        assertNull(response.getCompletedAt());
    }
}
