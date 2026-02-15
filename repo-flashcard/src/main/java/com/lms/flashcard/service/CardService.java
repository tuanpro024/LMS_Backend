package com.lms.flashcard.service;

import com.lms.flashcard.dto.request.CreateCardRequest;
import com.lms.flashcard.dto.request.UpdateCardRequest;
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

    List<CardResponse> addCardsToStudySet(String studySetId, List<CreateCardRequest> cards, String userId);

    List<CardResponse> getCardsByStudySetId(String studySetId);

    CardResponse getCardById(String id);

    CardResponse updateCard(String id, UpdateCardRequest request, String userId);

    void deleteCard(String id, String userId);
}
