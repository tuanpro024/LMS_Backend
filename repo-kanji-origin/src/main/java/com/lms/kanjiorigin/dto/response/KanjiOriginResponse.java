package com.lms.kanjiorigin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanjiOriginResponse {
    private String id;
    private String kanjiLessonId;
    private String term;
    private String pinyin;
    private String sinoVn;
    private String meaning;
    private String audioUrl;
    private String strokeAnimationUrl;
    private String originImage;
    private String originTextVi;
    private String originTextCn;
    private String originTextEn;
    private String exampleSentence;
    private String exampleMeaningVi;
    private String exampleMeaningEn;
    private String examplePinyin;
}
