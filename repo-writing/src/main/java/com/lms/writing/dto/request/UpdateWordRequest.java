package com.lms.writing.dto.request;

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

    private String imageUrl;

    private String example;

    private String examplePinyin;

    private String exampleMeaning;
}
