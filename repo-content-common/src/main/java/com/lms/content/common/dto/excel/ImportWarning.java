package com.lms.content.common.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a warning during import process
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportWarning {

    private int rowNumber;
    private WarningType type;
    private String message;
    private String details;

    public enum WarningType {
        MISSING_PACKAGE_NAME,
        MISSING_FOLDER_NAME,
        MISSING_STUDY_SET_NAME,
        MISSING_TERM,
        MISSING_DEFINITION,
        ORPHAN_CONTENT_ITEM,
        ORPHAN_STUDY_SET,
        ORPHAN_FOLDER,
        EMPTY_ROW
    }

    public static ImportWarning of(int rowNumber, WarningType type, String message) {
        return ImportWarning.builder()
                .rowNumber(rowNumber)
                .type(type)
                .message(message)
                .build();
    }
}
