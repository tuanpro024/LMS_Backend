package com.lms.learningpath.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Folder data. Can be used for both local folders and external module
 * folders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDto {
    private String id;
    private String name;
    private String description;
    private String thumbnail;
    private Boolean isPrivate;
    private String userId;
    private String packageId;
    private List<StudySetDto> studySets;
}
