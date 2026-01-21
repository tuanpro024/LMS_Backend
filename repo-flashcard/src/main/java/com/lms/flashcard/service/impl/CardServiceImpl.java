package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.UpdateCardStatusRequest;
import com.lms.flashcard.entity.Card;
import com.lms.flashcard.entity.UserCardProgress;
import com.lms.flashcard.repository.CardRepository;
import com.lms.flashcard.repository.UserCardProgressRepository;
import com.lms.flashcard.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserCardProgressRepository userCardProgressRepository;

    @Override
    public void updateCardStatus(String cardId, UpdateCardStatusRequest request, String userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Card not found"));

        // Find or create UserCardProgress for this user and card
        UserCardProgress progress = userCardProgressRepository
                .findByUserIdAndCardId(userId, cardId)
                .orElse(UserCardProgress.builder()
                        .userId(userId)
                        .card(card)
                        .status(request.getStatus())
                        .reviewCount(0)
                        .build());

        // Update status
        progress.setStatus(request.getStatus());
        progress.setLastReviewedAt(Instant.now());
        progress.setReviewCount(progress.getReviewCount() + 1);

        userCardProgressRepository.save(progress);
        
        log.info("Updated card progress for user {} on card {}: status={}", userId, cardId, request.getStatus());
    }
}
