package com.lms.dictionary.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVocabularyRequest {

    private Integer hskLevel;

    @NotBlank(message = "Hanzi is required")
    private String hanzi;

    @NotBlank(message = "Pinyin is required")
    private String pinyin;

    private String audioUrl;
    private String strokeAnimationUrl;
    private String etymologyStory;
    private String etymologyImage;

    @NotNull(message = "isSingleVocab must be specified")
    private Boolean isSingleVocab;

    @NotEmpty(message = "At least one meaning is required")
    @Valid
    private List<VocabularyMeaningRequest> meanings;

    @Valid
    private List<VocabComponentRequest> components;

    private List<Long> componentIds;
}
