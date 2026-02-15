package com.lms.learningpath.dto.excel;

import com.lms.learningpath.entity.enums.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents one row from the "Structure" sheet in Learning Path Excel import.
 * Follows hierarchical pattern similar to AbstractHierarchicalImportService.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathStructureRow {
    
    private int rowNumber;

    // Package (columns A-B)
    private String packageName;
    private String packageDescription;

    // Folder (columns C-D)
    private String folderName;
    private String folderDescription;

    // StudySet (columns E-F)
    private String studySetName;
    private String studySetDescription;

    // LearningPath (columns G-H)
    private String learningPathTitle;
    private String learningPathDescription;

    // Step (columns I-K)
    private String stepTitle;
    private String stepDescription;
    private Integer stepOrder;

    // Module (columns L-Q)
    private String moduleTitle;
    private String moduleType;          // String → parse to ModuleType enum
    private Integer moduleOrder;
    private Boolean isRequired;
    private String contentSheetName;    // Name of sheet containing content data
    private String existingContentSetId; // Pre-existing contentSetId to reuse

    /**
     * Helper methods to detect hierarchy level changes
     */
    public boolean hasPackageData() {
        return packageName != null && !packageName.isBlank();
    }

    public boolean hasFolderData() {
        return folderName != null && !folderName.isBlank();
    }

    public boolean hasStudySetData() {
        return studySetName != null && !studySetName.isBlank();
    }

    public boolean hasLearningPathData() {
        return learningPathTitle != null && !learningPathTitle.isBlank();
    }

    public boolean hasStepData() {
        return stepTitle != null && !stepTitle.isBlank();
    }

    public boolean hasModuleData() {
        return moduleTitle != null && !moduleTitle.isBlank();
    }

    public boolean isEmpty() {
        return !hasPackageData() && !hasFolderData() && !hasStudySetData()
                && !hasLearningPathData() && !hasStepData() && !hasModuleData();
    }

    /**
     * Parse moduleType string to ModuleType enum
     */
    public ModuleType getModuleTypeEnum() {
        if (moduleType == null || moduleType.isBlank()) {
            return null;
        }
        try {
            return ModuleType.valueOf(moduleType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
