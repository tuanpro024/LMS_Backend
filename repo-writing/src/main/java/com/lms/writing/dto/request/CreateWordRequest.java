package com.lms.writing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWordRequest {

    @NotBlank(message = "Word is required")
    @Size(max = 50, message = "Word must not exceed 50 characters")
    private String word;

    @NotBlank(message = "Pinyin is required")
    @Size(max = 200, message = "Pinyin must not exceed 200 characters")
    private String pinyin;

    @NotBlank(message = "Meaning is required")
    @Size(max = 500, message = "Meaning must not exceed 500 characters")
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

    private String audio;
}
