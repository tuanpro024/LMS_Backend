package com.lms.writing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportWarning {

    private int rowNumber;
    private String message;
    private WarningType type;

    public enum WarningType {
        MISSING_DEFINITION,
        MISSING_TERM,
        EMPTY_ROW,
        MISSING_STUDY_SET_NAME,
        INVALID_DATA
    }
}
