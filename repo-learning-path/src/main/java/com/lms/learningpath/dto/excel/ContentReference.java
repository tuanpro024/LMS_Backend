package com.lms.learningpath.dto.excel;

import com.lms.learningpath.entity.enums.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tracks content created in external repositories during Phase 1 import.
 * Used to link StepModules to external content in Phase 2.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReference {
    
    /**
     * Type of content module (FLASHCARD, WRITING, KANJI, QUIZ, etc.)
     */
    private ModuleType moduleType;
    
    /**
     * StudySet ID from the external repository
     */
    private String contentSetId;
    
    /**
     * Optional: Folder ID from external repository
     */
    private String contentFolderId;
    
    /**
     * Repository name (e.g., "repo-flashcard", "repo-writing")
     */
    private String repoName;
    
    /**
     * true = content was newly created in Phase 1
     * false = reused existing content (found via duplicate check or explicit ID)
     */
    private boolean newlyCreated;
}
