package com.lms.flashcard.service;

import com.lms.flashcard.dto.request.UpdateCardStatusRequest;

public interface CardService {

    void updateCardStatus(String cardId, UpdateCardStatusRequest request);
}
