package com.lms.aipractice.dto.response;

import com.lms.aipractice.entity.enums.AiItemSubtype;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AiPracticeItemResponse {
    private String id;
    private String studySetId;
    private AiItemSubtype questionSubtype;
    private Integer hskLevel;
    private String partLevel;
    private String promptText;
    private String imageDescription;
    private String requiredWordsJson;
    private String referenceAnswer;
    private String originalArticleSummary;
    private String referenceText;
    private String extraConfigJson;
    private Integer contentIndex;
    private Instant createdAt;
    private Instant updatedAt;
}
