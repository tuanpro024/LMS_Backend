package com.lms.learningpath.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Overall result of Learning Path Excel import operation.
 * Contains counters, IDs, content import details, warnings, and errors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathImportResult {

    // ========== Counters ==========
    private int totalPackages;
    private int totalFolders;
    private int totalStudySets;
    private int totalLearningPaths;
    private int totalSteps;
    private int totalModules;

    // ========== Entity IDs Created ==========
    @Builder.Default
    private List<String> packageIds = new ArrayList<>();

    @Builder.Default
    private List<String> folderIds = new ArrayList<>();

    @Builder.Default
    private List<String> studySetIds = new ArrayList<>();

    @Builder.Default
    private List<String> learningPathIds = new ArrayList<>();

    @Builder.Default
    private List<String> stepIds = new ArrayList<>();

    @Builder.Default
    private List<String> stepModuleIds = new ArrayList<>();

    // ========== Content Import Details ==========
    /**
     * Map of content sheet name → import details
     */
    @Builder.Default
    private Map<String, ContentImportDetail> contentImportResults = new LinkedHashMap<>();

    /**
     * List of all external content set IDs created (for potential cleanup if
     * needed)
     */
    @Builder.Default
    private List<String> externalContentSetIds = new ArrayList<>();

    // ========== Warnings & Errors ==========
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * Summary message
     */
    private String message;

    // ========== Helper Methods ==========
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
