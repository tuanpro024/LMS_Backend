package com.lms.pronunciation.dto.request;

import com.lms.pronunciation.entity.enums.PronunciationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePronunciationItemRequest {

    @NotNull(message = "Study Set ID is required")
    private String studySetId;

    private Integer contentIndex;

    @NotNull(message = "Type is required")
    private PronunciationType type;

    @NotBlank(message = "Symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
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
