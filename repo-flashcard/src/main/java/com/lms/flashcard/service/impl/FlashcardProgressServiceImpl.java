package com.lms.flashcard.service.impl;

import com.lms.flashcard.dto.response.FlashcardStudySetProgressResponse;
import com.lms.flashcard.entity.FlashcardStudySetProgress;
import com.lms.flashcard.entity.enums.CardStatus;
import com.lms.flashcard.entity.enums.ProgressStatus;
import com.lms.flashcard.repository.CardRepository;
import com.lms.flashcard.repository.FlashcardStudySetProgressRepository;
import com.lms.flashcard.repository.UserCardProgressRepository;
import com.lms.flashcard.service.FlashcardProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardProgressServiceImpl implements FlashcardProgressService {

    private final FlashcardStudySetProgressRepository progressRepository;
    private final UserCardProgressRepository cardProgressRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public FlashcardStudySetProgressResponse updateStudySetProgress(String userId, String studySetId) {
        log.info("Updating study set progress for user {} and study set {}", userId, studySetId);

        long totalCards = cardRepository.countByStudySetIdAndDeletedFalse(studySetId);
        long learnedCards = cardProgressRepository.countByUserIdAndStudySetIdAndStatus(userId, studySetId, CardStatus.LEARNED);

        ProgressStatus status;
        if (totalCards == 0) {
            status = ProgressStatus.NOT_STARTED;
        } else if (learnedCards == 0) {
            status = ProgressStatus.NOT_STARTED;
        } else if (learnedCards >= totalCards) {
            status = ProgressStatus.COMPLETED;
        } else {
            status = ProgressStatus.IN_PROGRESS;
        }

        FlashcardStudySetProgress progress = progressRepository.findByUserIdAndStudySetId(userId, studySetId)
                .orElse(FlashcardStudySetProgress.builder()
                        .userId(userId)
                        .studySetId(studySetId)
                        .status(ProgressStatus.NOT_STARTED)
                        .learnedCards(0)
                        .totalCards((int) totalCards)
                        .progressPercentage(0.0)
                        .build());

        progress.setTotalCards((int) totalCards);
        progress.setLearnedCards((int) learnedCards);
        progress.setStatus(status);
        progress.updateProgressPercent();

        if (progress.getFirstStartedAt() == null && learnedCards > 0) {
            progress.setFirstStartedAt(Instant.now());
        }

        if (status == ProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
        } else if (status != ProgressStatus.COMPLETED) {
            // If it was completed before but now it's not (e.g. user unlearned a card)
            progress.setCompletedAt(null);
        }

        progress = progressRepository.save(progress);

        return toResponse(progress);
    }

    @Override
    public FlashcardStudySetProgressResponse getStudySetProgress(String userId, String studySetId) {
        return progressRepository.findByUserIdAndStudySetId(userId, studySetId)
                .map(this::toResponse)
                .orElse(null);
    }

    private FlashcardStudySetProgressResponse toResponse(FlashcardStudySetProgress p) {
        return FlashcardStudySetProgressResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .studySetId(p.getStudySetId())
                .status(p.getStatus())
                .learnedCards(p.getLearnedCards())
                .totalCards(p.getTotalCards())
                .progressPercentage(p.getProgressPercentage())
                .firstStartedAt(p.getFirstStartedAt())
                .completedAt(p.getCompletedAt())
                .build();
    }
}
