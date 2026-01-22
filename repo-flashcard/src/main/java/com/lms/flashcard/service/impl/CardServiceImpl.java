package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.UpdateCardStatusRequest;
import com.lms.flashcard.dto.response.CardResponse;
import com.lms.flashcard.entity.Card;
import com.lms.flashcard.entity.UserCardProgress;
import com.lms.flashcard.entity.enums.CardStatus;
import com.lms.flashcard.mapper.CardMapper;
import com.lms.flashcard.repository.CardRepository;
import com.lms.flashcard.repository.UserCardProgressRepository;
import com.lms.flashcard.repository.UserCardProgressRepository;
import com.lms.flashcard.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserCardProgressRepository userCardProgressRepository;
    private final CardMapper cardMapper;

    @Override
    public void updateCardStatus(String userId, String cardId, UpdateCardStatusRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Card not found"));

        UserCardProgress progress = userCardProgressRepository.findByUserIdAndCardId(userId, cardId)
                .orElse(UserCardProgress.builder()
                        .userId(userId)
                        .card(card)
                        .status(CardStatus.NOT_LEARNED)
                        .build());

        progress.setStatus(request.getStatus());
        progress.setLastReviewedAt(java.time.Instant.now());
        if (request.getStatus() == CardStatus.LEARNED) {
            progress.setReviewCount(progress.getReviewCount() + 1);
        }

        userCardProgressRepository.save(progress);
    }

    @Override
    public List<CardResponse> getLearnedCards(String userId, String studySetId) {
        List<String> cardIds = userCardProgressRepository.findCardIdsByUserIdAndStudySetIdAndStatus(userId, studySetId,
                CardStatus.LEARNED);
        List<Card> cards = cardRepository.findAllById(cardIds);
        List<CardResponse> responses = cardMapper.toResponseList(cards);
        responses.forEach(r -> r.setStatus(CardStatus.LEARNED.name()));
        return responses;
    }

    @Override
    public List<CardResponse> getNotLearnedCards(String userId, String studySetId) {
        List<Card> allCards = cardRepository.findByStudySetId(studySetId);
        List<String> learnedCardIds = userCardProgressRepository.findCardIdsByUserIdAndStudySetIdAndStatus(userId,
                studySetId, CardStatus.LEARNED);

        List<Card> notLearnedCards = allCards.stream()
                .filter(c -> !learnedCardIds.contains(c.getId()))
                .toList();

        List<CardResponse> responses = cardMapper.toResponseList(notLearnedCards);
        responses.forEach(r -> r.setStatus(CardStatus.NOT_LEARNED.name()));
        return responses;
    }

    @Override
    public long countLearnedCards(String userId, String studySetId) {
        return userCardProgressRepository.countByUserIdAndStudySetIdAndStatus(userId, studySetId, CardStatus.LEARNED);
    }

    @Override
    public long countNotLearnedCards(String userId, String studySetId) {
        long total = cardRepository.countByStudySetId(studySetId);
        long learned = countLearnedCards(userId, studySetId);
        return total - learned;
    }

    @Override
    public long countTotalCards(String studySetId) {
        return cardRepository.countByStudySetId(studySetId);
    }
}
