package com.lms.learningpath.dto.response;

import com.lms.learningpath.entity.enums.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for SectionModule with enriched StudySet data from external
 * services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionModuleResponse {
    private String id;
    private String folderId;
    private ModuleType moduleType;
    private String studySetId;
    private Integer moduleOrder;
    private String displayTitle;
    private Boolean isRequired;
    private String moduleDescription;

    // Enriched data from external StudySet
    private String studySetTitle;
    private String studySetDescription;
    private String studySetThumbnail;
    private Integer estimatedMinutes;
    private Long itemCount;
    private Boolean isAvailable; // Whether the StudySet still exists in the module
}
