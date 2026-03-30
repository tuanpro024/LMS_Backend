package com.lms.content.common.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of hierarchical import from Excel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchicalImportResult {

    @Builder.Default
    private List<String> packageIds = new ArrayList<>();

    @Builder.Default
    private List<String> folderIds = new ArrayList<>();

    @Builder.Default
    private List<String> studySetIds = new ArrayList<>();

    private int totalPackages;
    private int totalFolders;
    private int totalStudySets;
    private int totalContentItems;

    @Builder.Default
    private List<ImportWarning> warnings = new ArrayList<>();

    private String message;

    public void addWarning(ImportWarning warning) {
        this.warnings.add(warning);
    }

    public void addWarning(int rowNumber, ImportWarning.WarningType type, String message) {
        this.warnings.add(ImportWarning.of(rowNumber, type, message));
    }
}
