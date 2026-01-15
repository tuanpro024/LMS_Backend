package com.lms.flashcard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HanziTermResponse {

    private String cardId;
    private String originalTerm;
    private List<String> characters;
    private String pinyin;
}
