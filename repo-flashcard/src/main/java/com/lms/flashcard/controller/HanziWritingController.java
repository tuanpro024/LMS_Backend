package com.lms.flashcard.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.flashcard.dto.response.HanziTermResponse;
import com.lms.flashcard.dto.response.HanziWritingResponse;
import com.lms.flashcard.service.HanziWritingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hanzi-writing")
@RequiredArgsConstructor
public class HanziWritingController {

    private final HanziWritingService hanziWritingService;

    /**
     * Get all cards from a study set with terms split into individual characters
     * for use with Hanzi Writer library
     *
     * Example response:
     * {
     * "studySetId": "abc123",
     * "studySetTitle": "HSK 1 Vocabulary",
     * "terms": [
     * {
     * "cardId": "card1",
     * "originalTerm": "你好",
     * "characters": ["你", "好"],
     * "definition": "hello",
     * "pinyin": "nǐ hǎo"
     * }
     * ]
     * }
     *
     * @param studySetId The ID of the study set
     * @return HanziWritingResponse containing the study set info and terms split
     *         into characters
     */
    @GetMapping("/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<HanziWritingResponse>> getHanziCharactersForStudySet(
            @PathVariable String studySetId) {

        HanziWritingResponse response = hanziWritingService.getHanziCharactersForStudySet(studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get hanzi characters for a single card
     *
     * Example response:
     * {
     * "cardId": "card1",
     * "originalTerm": "你好",
     * "characters": ["你", "好"],
     * "definition": "hello",
     * "pinyin": "nǐ hǎo"
     * }
     *
     * @param cardId The ID of the card
     * @return HanziTermResponse containing the card info and characters
     */
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<ApiResponse<HanziTermResponse>> getHanziCharactersForCard(
            @PathVariable String cardId) {

        HanziTermResponse response = hanziWritingService.getHanziCharactersForCard(cardId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
