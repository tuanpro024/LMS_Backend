package com.lms.dictionary.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyMeaningRequest {

    @NotBlank(message = "Meaning is required")
    private String meaning;

    private String exampleSentenceCn;
    private String exampleSentenceVi;
    private String exampleSentenceEn;
    private String exampleSentencePinyin;
}
