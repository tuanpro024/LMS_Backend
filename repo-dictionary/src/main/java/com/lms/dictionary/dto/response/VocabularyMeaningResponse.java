package com.lms.dictionary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyMeaningResponse {
    private Long id;
    private String meaning;
    private String exampleSentenceCn;
    private String exampleSentenceVi;
    private String exampleSentenceEn;
    private String exampleSentencePinyin;
}
