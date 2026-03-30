package com.lms.content.common.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a row in hierarchical Excel import.
 * Structure: Package -&gt; Folder -&gt; StudySet -&gt; Content Items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchicalImportRow {

    private int rowNumber;

    // Package level
    private String packageName;
    private String packageDescription;

    // Folder level
    private String folderName;
    private String folderDescription;

    // StudySet level
    private String studySetName;
    private String studySetDescription;

    // Content Item level
    private String term;
    private String definition;
    private String pinyin;
    private String sinoVn;
    private String wordType;
    private String hskLevel;
    private String imageWord;
    private String sinoOrigin;
    private String imageOrigin;
    private String audio;
    private String exampleSentence;
    private String examplePinyin;
    private String exampleMeaning;
    private String charactersJson;

    public boolean hasPackageData() {
        return packageName != null && !packageName.trim().isEmpty();
    }

    public boolean hasFolderData() {
        return folderName != null && !folderName.trim().isEmpty();
    }

    public boolean hasStudySetData() {
        return studySetName != null && !studySetName.trim().isEmpty();
    }

    public boolean hasContentItemData() {
        return term != null && !term.trim().isEmpty();
    }

    public boolean isEmpty() {
        return !hasPackageData() && !hasFolderData() && !hasStudySetData() && !hasContentItemData();
    }
}
