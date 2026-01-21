package com.lms.writing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResponse {

    private String folderId;
    private String folderName;
    private int totalStudySets;
    private int totalWords;

    @Builder.Default
    private List<String> studySetIds = new ArrayList<>();

    @Builder.Default
    private List<ImportWarning> warnings = new ArrayList<>();

    private String message;
}
