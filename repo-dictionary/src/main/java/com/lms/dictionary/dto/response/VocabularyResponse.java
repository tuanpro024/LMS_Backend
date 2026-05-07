package com.lms.dictionary.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyResponse {
    private Long id;
    private Integer hskLevel;
    private List<String> wordTypes;
    private String hanzi;
    private String pinyin;
    private String audioUrl;
    private String imageUrl;
    private String strokeAnimationUrl;
    private String etymologyStory;
    private String etymologyImage;
    private String radical;
    private Integer strokeCount;
    private String strokes;
    private Boolean isSingleVocab;
    private List<VocabularyMeaningResponse> meanings;
    private List<VocabularyResponse> componentVocabs;
}
