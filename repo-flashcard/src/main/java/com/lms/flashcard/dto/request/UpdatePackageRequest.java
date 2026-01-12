package com.lms.flashcard.dto.request;

import com.lms.flashcard.entity.PackageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePackageRequest {

    private String name;

    private String subjectCode;

    private String slot;

    private PackageType type;

    private String description;
}
