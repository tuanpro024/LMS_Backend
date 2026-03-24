package com.lms.flashcard.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.flashcard.dto.request.CreateCardRequest;
import com.lms.flashcard.dto.request.UpdateCardRequest;
import com.lms.flashcard.dto.request.UpdateCardStatusRequest;
import com.lms.flashcard.dto.response.CardResponse;
import com.lms.flashcard.entity.Card;
import com.lms.flashcard.entity.UserCardProgress;
import com.lms.flashcard.entity.enums.CardStatus;
import com.lms.flashcard.mapper.CardMapper;
import com.lms.flashcard.repository.CardRepository;
import com.lms.flashcard.repository.UserCardProgressRepository;
import com.lms.flashcard.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserCardProgressRepository userCardProgressRepository;
    private final CardMapper cardMapper;
    private final StudySetRepository studySetRepository;
    private final ObjectMapper objectMapper;
    private final com.lms.flashcard.service.FlashcardProgressService flashcardProgressService;

    @Override
    public void updateCardStatus(String userId, String cardId, UpdateCardStatusRequest request) {
        Card card = cardRepository.findByIdAndDeletedFalse(cardId)
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

        // Update StudySet progress
        if (card.getStudySet() != null) {
            flashcardProgressService.updateStudySetProgress(userId, card.getStudySet().getId());
        }
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
        List<Card> allCards = cardRepository.findByStudySetIdAndDeletedFalse(studySetId);
        Set<String> learnedCardIds = new HashSet<>(
                userCardProgressRepository.findCardIdsByUserIdAndStudySetIdAndStatus(userId,
                        studySetId, CardStatus.LEARNED));

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
        long total = cardRepository.countByStudySetIdAndDeletedFalse(studySetId);
        long learned = countLearnedCards(userId, studySetId);
        return total - learned;
    }

    @Override
    public long countTotalCards(String studySetId) {
        return cardRepository.countByStudySetIdAndDeletedFalse(studySetId);
    }

    @Override
    public List<CardResponse> addCardsToStudySet(String studySetId, List<CreateCardRequest> cards, String userId) {
        // Validate StudySet exists
        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        log.debug("Adding cards to StudySet: studySetId={}, studySetOwnerId={}, requestUserId={}",
                studySetId, studySet.getUserId(), userId);

        // Check ownership — only the study set owner can add cards
        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add cards to this study set");
        }

        // Convert and save all cards
        List<Card> cardEntities = cards.stream()
                .map(request -> {
                    Card card = cardMapper.toEntity(request);
                    card.setStudySet(studySet);

                    // Handle characters JSON conversion
                    if (request.getCharacters() != null && !request.getCharacters().isEmpty()) {
                        try {
                            String charactersJson = objectMapper.writeValueAsString(request.getCharacters());
                            card.setCharacters(charactersJson);
                        } catch (Exception e) {
                            log.error("Error converting characters to JSON", e);
                        }
                    }

                    return card;
                })
                .collect(Collectors.toList());

        List<Card> savedCards = cardRepository.saveAll(cardEntities);

        log.info("Added {} cards to StudySet {} by user {}", savedCards.size(), studySetId, userId);

        // Update StudySet progress
        flashcardProgressService.updateStudySetProgress(userId, studySetId);

        return cardMapper.toResponseList(savedCards);
    }

    @Override
    public List<CardResponse> getCardsByStudySetId(String studySetId) {
        List<Card> cards = cardRepository.findByStudySetIdAndDeletedFalse(studySetId);
        return cardMapper.toResponseList(cards);
    }

    @Override
    public CardResponse getCardById(String id) {
        Card card = cardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Card not found"));
        return cardMapper.toResponse(card);
    }

    @Override
    public CardResponse updateCard(String id, UpdateCardRequest request, String userId) {
        Card card = cardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Card not found"));

        // Verify ownership
        StudySet studySet = card.getStudySet();
        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E228, "You don't have permission to update this card");
        }

        // Update fields if provided
        if (request.getTerm() != null) {
            card.setTerm(request.getTerm());
        }
        if (request.getDefinition() != null) {
            card.setDefinition(request.getDefinition());
        }
        if (request.getCardIndex() != null) {
            card.setCardIndex(request.getCardIndex());
        }
        if (request.getPinyin() != null) {
            card.setPinyin(request.getPinyin());
        }
        if (request.getExampleSentence() != null) {
            card.setExampleSentence(request.getExampleSentence());
        }
        if (request.getSinoVn() != null) {
            card.setSinoVn(request.getSinoVn());
        }
        if (request.getWordType() != null) {
            card.setWordType(request.getWordType());
        }
        if (request.getHskLevel() != null) {
            card.setHskLevel(request.getHskLevel());
        }
        if (request.getExamplePinyin() != null) {
            card.setExamplePinyin(request.getExamplePinyin());
        }
        if (request.getExampleMeaning() != null) {
            card.setExampleMeaning(request.getExampleMeaning());
        }
        if (request.getSinoOrigin() != null) {
            card.setSinoOrigin(request.getSinoOrigin());
        }
        if (request.getImageWord() != null) {
            card.setImageWord(request.getImageWord());
        }
        if (request.getImageOrigin() != null) {
            card.setImageOrigin(request.getImageOrigin());
        }
        if (request.getAudio() != null) {
            card.setAudio(request.getAudio());
        }
        if (request.getCharacters() != null) {
            try {
                String charactersJson = objectMapper.writeValueAsString(request.getCharacters());
                card.setCharacters(charactersJson);
            } catch (Exception e) {
                log.error("Error converting characters to JSON", e);
            }
        }

        Card savedCard = cardRepository.save(card);
        log.info("Updated card {} by user {}", id, userId);

        return cardMapper.toResponse(savedCard);
    }

    @Override
    public void deleteCard(String id, String userId) {
        Card card = cardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Card not found"));

        // Verify ownership
        StudySet studySet = card.getStudySet();
        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E228, "You don't have permission to delete this card");
        }

        // Delete associated user progress records first to avoid FK constraint
        // violation
        userCardProgressRepository.deleteByCardId(id);

        String studySetId = card.getStudySet().getId();
        cardRepository.delete(card);
        log.info("Deleted card {} by user {}", id, userId);

        // Update StudySet progress
        flashcardProgressService.updateStudySetProgress(userId, studySetId);
    }
}
