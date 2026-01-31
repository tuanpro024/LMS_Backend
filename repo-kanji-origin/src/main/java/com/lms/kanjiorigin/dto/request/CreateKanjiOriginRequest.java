package com.lms.kanjiorigin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKanjiOriginRequest {

    @NotNull(message = "Kanji Lesson ID is required")
    private String kanjiLessonId;

    @NotBlank(message = "Term is required")
    private String term;

    @NotBlank(message = "Pinyin is required")
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
