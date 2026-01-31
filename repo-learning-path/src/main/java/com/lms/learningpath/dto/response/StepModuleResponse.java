package com.lms.learningpath.dto.response;

import com.lms.learningpath.entity.enums.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepModuleResponse {

    private String id;
    private String stepId;
    private ModuleType moduleType;
    private Integer moduleOrder;
    private String title;
    private String description;
    private String contentSetId;
    private String contentFolderId;
    private String externalRefJson;
    private Boolean isRequired;
    private Boolean isActive;
    private Instant createdDate;
    private Instant lastModifiedDate;

    // Additional external content details (fetched from other repos)
    private ExternalContentDetails externalContent;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalContentDetails {
        private String setTitle;
        private String setDescription;
        private String setThumbnail;
        private String repoName; // "flashcard", "writing", "kanji-origin"
        private Integer itemCount;
    }
}
