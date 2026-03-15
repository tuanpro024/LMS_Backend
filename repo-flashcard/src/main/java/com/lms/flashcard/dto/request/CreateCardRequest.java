package com.lms.flashcard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCardRequest {

    @NotBlank(message = "Term is required")
    @Size(max = 100, message = "Term must not exceed 100 characters")
    private String term;

    @NotBlank(message = "Definition is required")
    @Size(max = 500, message = "Definition must not exceed 500 characters")
    private String definition;

    @NotNull(message = "Index is required")
    private Integer cardIndex;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterInfo {
        private String hanzi;
        private String radicals;
    }
}
