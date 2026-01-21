package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.UpdateCardStatusRequest;
import com.lms.flashcard.dto.response.CardResponse;
import com.lms.flashcard.entity.Card;
import com.lms.flashcard.entity.enums.CardStatus;
import com.lms.flashcard.mapper.CardMapper;
import com.lms.flashcard.repository.CardRepository;
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
    private final CardMapper cardMapper;

    @Override
    public void updateCardStatus(String cardId, UpdateCardStatusRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Card not found"));

        // Convert CardStatus to ContentStatus
        com.lms.content.common.entity.enums.ContentStatus contentStatus = com.lms.content.common.entity.enums.ContentStatus
                .valueOf(request.getStatus().name());
        card.setStatus(contentStatus);
        cardRepository.save(card);
    }

    @Override
    public List<CardResponse> getLearnedCards(String studySetId) {
        List<Card> cards = cardRepository.findByStudySetIdAndStatus(studySetId, CardStatus.LEARNED);
        return cardMapper.toResponseList(cards);
    }

    @Override
    public List<CardResponse> getNotLearnedCards(String studySetId) {
        List<Card> cards = cardRepository.findByStudySetIdAndStatus(studySetId, CardStatus.NOT_LEARNED);
        return cardMapper.toResponseList(cards);
    }

    @Override
    public long countLearnedCards(String studySetId) {
        return cardRepository.countByStudySetIdAndStatus(studySetId, CardStatus.LEARNED);
    }

    @Override
    public long countNotLearnedCards(String studySetId) {
        return cardRepository.countByStudySetIdAndStatus(studySetId, CardStatus.NOT_LEARNED);
    }

    @Override
    public long countTotalCards(String studySetId) {
        return cardRepository.countByStudySetId(studySetId);
    }
}
