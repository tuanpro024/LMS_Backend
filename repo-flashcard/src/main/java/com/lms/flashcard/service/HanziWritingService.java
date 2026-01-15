package com.lms.flashcard.service;

import com.lms.flashcard.dto.response.HanziTermResponse;
import com.lms.flashcard.dto.response.HanziWritingResponse;

public interface HanziWritingService {

    /**
     * Get all cards from a study set with terms split into individual characters
     * for use with Hanzi Writer library
     *
     * @param studySetId The ID of the study set
     * @return HanziWritingResponse containing the study set info and terms split
     *         into characters
     */
    HanziWritingResponse getHanziCharactersForStudySet(String studySetId);

    /**
     * Get hanzi characters for a single card
     *
     * @param cardId The ID of the card
     * @return HanziTermResponse containing the card info and characters
     */
    HanziTermResponse getHanziCharactersForCard(String cardId);

}
