package com.lms.videocourse.dto.response;

import com.lms.videocourse.entity.enums.ModuleType;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for available study set modules from other microservices.
 * Used by Admin/Teacher when selecting study sets to associate with video
 * steps.
 */
@Data
@Builder
public class AvailableModuleResponse { // Diagnostic touch 1

    private String id;
    private String title;
    private String description;
    private String thumbnail;

    /** Type of module: FLASHCARD, WRITING, KANJI, QUIZ */
    private ModuleType moduleType;

    /** Which microservice this study set comes from */
    private String repoName;

    private String folderId;
    private String folderName;

    private Long itemCount;
    private boolean isPrivate;
    private String userId;
}
