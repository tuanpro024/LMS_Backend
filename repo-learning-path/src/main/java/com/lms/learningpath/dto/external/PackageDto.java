package com.lms.learningpath.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Package data. Can be used for both local packages and external module
 * packages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageDto {
    private String id;
    private String name;
    private String description;
    private String thumbnail;
    private String userId;
    private String typeId;
    private String typeName;
    private List<FolderDto> folders;
}
