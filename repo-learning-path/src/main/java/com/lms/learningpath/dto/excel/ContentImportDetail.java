package com.lms.learningpath.dto.excel;

import com.lms.learningpath.entity.enums.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Details about the import result for one content sheet.
 * Tracks whether content was created, reused, or failed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentImportDetail {

    /**
     * Name of the content sheet
     */
    private String sheetName;

    /**
     * Type of module content
     */
    private ModuleType moduleType;

    /**
     * StudySet ID (if successful)
     */
    private String contentSetId;

    /**
     * true if content was newly created
     */
    private boolean newlyCreated;

    /**
     * true if existing content was reused
     */
    private boolean reused;

    /**
     * true if import failed
     */
    private boolean failed;

    /**
     * Error message (if failed)
     */
    private String errorMessage;

    /**
     * Number of content items imported (cards, words, etc.)
     */
    private int itemCount;
}
