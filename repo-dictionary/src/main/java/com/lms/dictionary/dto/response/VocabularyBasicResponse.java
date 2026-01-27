package com.lms.dictionary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyBasicResponse {
    private Long id;
    private String hanzi;
    private String pinyin;
    private List<String> wordTypes;
    private List<String> meanings;
}
