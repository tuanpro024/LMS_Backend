package com.lms.pronunciation.dto.response;

import com.lms.pronunciation.entity.enums.PronunciationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PronunciationItemResponse {
    private String id;
    private String studySetId;
    private Integer contentIndex;
    private PronunciationType type;
    private String symbol;
    private String pinyin;
    private String hanzi;
    private String pronunciationGuide;
    private String mouthImageUrl;
    private String audioUrl;
    private String exampleWord;
    private String examplePinyin;
    private String exampleMeaning;
}
