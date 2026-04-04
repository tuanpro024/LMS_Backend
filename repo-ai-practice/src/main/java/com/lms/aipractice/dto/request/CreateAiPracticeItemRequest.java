package com.lms.aipractice.dto.request;

import com.lms.aipractice.entity.enums.AiItemSubtype;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAiPracticeItemRequest {

    @NotBlank
    private String studySetId;

    @NotNull
    private AiItemSubtype questionSubtype;

    private Integer hskLevel;
    private String partLevel;

    @NotBlank
    private String promptText;

    private String imageDescription;
    private String requiredWordsJson;     // JSON array: ["尽管","影响","坚持"]
    private String referenceAnswer;
    private String originalArticleSummary;
    private String referenceText;         // For AUDIO_COMPARE
    private String extraConfigJson;
    private Integer contentIndex;
}
