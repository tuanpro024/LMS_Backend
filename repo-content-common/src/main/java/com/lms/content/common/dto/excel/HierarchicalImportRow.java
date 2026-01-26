package com.lms.content.common.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a row in hierarchical Excel import
 * Structure: Package -> Subject -> Slot -> Folder -> StudySet -> Content Items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchicalImportRow {

    private int rowNumber;

    // Package level (Column A-B)
    private String packageName; // Column A
    private String packageDescription; // Column B

    // Subject level (Column C-E) - nullable
    private String subjectName; // Column C
    private String subjectCode; // Column D
    private String subjectDescription; // Column E

    // Slot level (Column F-H) - nullable
    private String slotName; // Column F
    private String slotNumber; // Column G
    private String slotDescription; // Column H

    // Folder level (Column I-K)
    private String folderName; // Column I
    private String folderDescription; // Column J
    private String folderColor; // Column K

    // StudySet level (Column L-M)
    private String studySetName; // Column L
    private String studySetDescription; // Column M

    // Content Item level (Column N onwards)
    private String term; // Column N (Word/Term)
    private String definition; // Column O (Meaning/Definition)
    private String pinyin; // Column P
    private String sinoVn; // Column Q
    private String wordType; // Column R
    private String hskLevel; // Column S
    private String imageWord; // Column T
    private String sinoOrigin; // Column U
    private String imageOrigin; // Column V
    private String exampleSentence; // Column W
    private String audio; // Column X
    private String examplePinyin; // Column Y
    private String exampleMeaning; // Column Z
    private String charactersJson; // Column AA

    /**
     * Check if this row starts a new Package
     */
    public boolean hasPackageData() {
        return packageName != null && !packageName.trim().isEmpty();
    }

    /**
     * Check if this row starts a new Subject
     */
    public boolean hasSubjectData() {
        return subjectName != null && !subjectName.trim().isEmpty();
    }

    /**
     * Check if this row starts a new Slot
     */
    public boolean hasSlotData() {
        return slotName != null && !slotName.trim().isEmpty();
    }

    /**
     * Check if this row starts a new Folder
     */
    public boolean hasFolderData() {
        return folderName != null && !folderName.trim().isEmpty();
    }

    /**
     * Check if this row starts a new StudySet
     */
    public boolean hasStudySetData() {
        return studySetName != null && !studySetName.trim().isEmpty();
    }

    /**
     * Check if this row has content item (Card/Word)
     */
    public boolean hasContentItemData() {
        return term != null && !term.trim().isEmpty();
    }

    /**
     * Check if this is an empty row
     */
    public boolean isEmpty() {
        return !hasPackageData() && !hasSubjectData() && !hasSlotData() &&
                !hasFolderData() && !hasStudySetData() && !hasContentItemData();
    }
}
