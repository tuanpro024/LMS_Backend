package com.lms.flashcard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private String id;
    private String term;
    private String definition;
    private int cardIndex;
    private String pinyin;
    private String exampleSentence;
    private String sinoVn;
    private String wordType;
    private String hskLevel;
    private String examplePinyin;
    private String exampleMeaning;
    private String sinoOrigin;
    private String imageWord;
    private String imageOrigin;
    private String audio;
    private List<CharacterInfo> characters;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterInfo {
        private String hanzi;
        private String radicals;
    }
}
