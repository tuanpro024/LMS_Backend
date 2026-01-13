package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.UpdateCardStatusRequest;
import com.lms.flashcard.entity.Card;
import com.lms.flashcard.repository.CardRepository;
import com.lms.flashcard.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Override
    public void updateCardStatus(String cardId, UpdateCardStatusRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Card not found"));

        card.setStatus(request.getStatus());
        cardRepository.save(card);
    }
}
