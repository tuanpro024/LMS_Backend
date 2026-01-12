package com.lms.flashcard.dto.request;

import com.lms.flashcard.entity.PackageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePackageRequest {

    @NotBlank(message = "Name is required")
    private String name;

    // @NotBlank(message = "Subject code is required")
    private String subjectCode;

    // @NotBlank(message = "Slot is required")
    private String slot;

    @NotNull(message = "Type is required")
    private PackageType type;

    private String description;
}
