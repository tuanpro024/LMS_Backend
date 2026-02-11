package com.lms.kanjiorigin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportKanjiRequest {
    private String term;
    private String pinyin;
    private String sinoVn;
    private String meaning;
    private String originImage;
    private String originTextVi;
    private String originTextCn;
    private String exampleSentence;
    private String exampleMeaning;
    private String examplePinyin;
}
