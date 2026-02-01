package com.lms.learningpath.dto.response;

import com.lms.learningpath.entity.enums.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for displaying available study sets from other repos (flashcard, writing,
 * kanji-origin)
 * Used when Admin/Teacher is selecting which modules to add to a step
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableModuleResponse {

    private String id; // StudySet ID
    private String title;
    private String description;
    private String thumbnail;
    private ModuleType moduleType; // FLASHCARD, WRITING, KANJI_ORIGIN, etc.
    private String repoName; // "repo-flashcard", "repo-writing", etc.
    private String folderId; // Optional folder ID
    private String folderName; // Optional folder name
    private Long itemCount; // Number of items in the study set
    private Boolean isPrivate;
    private String userId; // Owner
}
