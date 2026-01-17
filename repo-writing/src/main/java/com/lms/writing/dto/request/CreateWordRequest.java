package com.lms.writing.dto.request;

import jakarta.validation.constraints.NotBlank;
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
    private String word;

    @NotBlank(message = "Pinyin is required")
    private String pinyin;

    @NotBlank(message = "Meaning is required")
    private String meaning;

    private String imageUrl;

    private String example;

    private String examplePinyin;

    private String exampleMeaning;
}
