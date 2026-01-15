package com.lms.flashcard.service.impl;

import com.lms.flashcard.dto.response.HanziTermResponse;
import com.lms.flashcard.dto.response.HanziWritingResponse;
import com.lms.flashcard.entity.Card;
import com.lms.flashcard.entity.StudySet;
import com.lms.flashcard.repository.CardRepository;
import com.lms.flashcard.repository.StudySetRepository;
import com.lms.flashcard.service.HanziWritingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HanziWritingServiceImpl implements HanziWritingService {

    private final StudySetRepository studySetRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public HanziWritingResponse getHanziCharactersForStudySet(String studySetId) {
        // Fetch the study set
        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new RuntimeException("Study set not found with id: " + studySetId));

        // Process each card to extract and split terms
        List<HanziTermResponse> terms = new ArrayList<>();
        for (Card card : studySet.getCards()) {
            HanziTermResponse termResponse = HanziTermResponse.builder()
                    .cardId(card.getId())
                    .originalTerm(card.getTerm())
                    .characters(splitIntoCharacters(card.getTerm()))
                    .pinyin(card.getPinyin())
                    .build();
            terms.add(termResponse);
        }

        // Build and return the response
        return HanziWritingResponse.builder()
                .studySetId(studySet.getId())
                .studySetTitle(studySet.getTitle())
                .terms(terms)
                .build();
    }

    /**
     * Split a term string into individual characters
     * Example: "你好" -> ["你", "好"]
     * Example: "学" -> ["学"]
     *
     * @param term The term to split
     * @return List of individual characters
     */
    private List<String> splitIntoCharacters(String term) {
        if (term == null || term.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> characters = new ArrayList<>();
        // Split the string into individual characters (including multi-byte characters
        // like Chinese)
        for (int i = 0; i < term.length(); i++) {
            char c = term.charAt(i);
            // Skip whitespace characters
            if (!Character.isWhitespace(c)) {
                characters.add(String.valueOf(c));
            }
        }

        return characters;
    }

    @Override
    @Transactional(readOnly = true)
    public HanziTermResponse getHanziCharactersForCard(String cardId) {
        // Fetch the card
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        // Build and return the response with characters split
        return HanziTermResponse.builder()
                .cardId(card.getId())
                .originalTerm(card.getTerm())
                .characters(splitIntoCharacters(card.getTerm()))
                .pinyin(card.getPinyin())
                .build();
    }
}
