package com.lms.flashcard.service;

import com.lms.flashcard.dto.request.UpdateCardStatusRequest;
import com.lms.flashcard.dto.response.CardResponse;

import java.util.List;

public interface CardService {

    void updateCardStatus(String cardId, UpdateCardStatusRequest request);

    List<CardResponse> getLearnedCards(String studySetId);

    List<CardResponse> getNotLearnedCards(String studySetId);

    long countLearnedCards(String studySetId);

    long countNotLearnedCards(String studySetId);

    long countTotalCards(String studySetId);
}
