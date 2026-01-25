package com.lms.writing.dto.request;

import com.lms.writing.entity.enums.WordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWordRequest {

    private String word;

    private String pinyin;

    private String meaning;

    private String sinoVn;

    private String wordType;

    private String hskLevel;

    private String example;

    private String examplePinyin;

    private String exampleMeaning;

    private String sinoOrigin;

    private String imageWord;

    private String imageOrigin;

    private WordStatus status;
}
