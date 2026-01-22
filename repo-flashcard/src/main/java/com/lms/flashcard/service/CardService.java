package com.lms.flashcard.service;

import com.lms.flashcard.dto.request.UpdateCardStatusRequest;
import com.lms.flashcard.dto.response.CardResponse;

import java.util.List;

public interface CardService {

    void updateCardStatus(String userId, String cardId, UpdateCardStatusRequest request);

    List<CardResponse> getLearnedCards(String userId, String studySetId);

    List<CardResponse> getNotLearnedCards(String userId, String studySetId);

    long countLearnedCards(String userId, String studySetId);

    long countNotLearnedCards(String userId, String studySetId);

    long countTotalCards(String studySetId);
}
