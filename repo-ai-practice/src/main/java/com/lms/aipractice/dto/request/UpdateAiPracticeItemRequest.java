package com.lms.aipractice.dto.request;

import com.lms.aipractice.entity.enums.AiItemSubtype;
import lombok.Data;

@Data
public class UpdateAiPracticeItemRequest {

    private AiItemSubtype questionSubtype;
    private Integer hskLevel;
    private String partLevel;
    private String promptText;
    private String imageDescription;
    private String requiredWordsJson;
    private String referenceAnswer;
    private String originalArticleSummary;
    private String referenceText;
    private String questionAudioFileId;
    private String questionImageFileId;
    private String extraConfigJson;
    private Integer contentIndex;
}
