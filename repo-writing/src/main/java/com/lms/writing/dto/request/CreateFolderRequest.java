package com.lms.writing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFolderRequest {

    @NotBlank(message = "Folder name is required")
    private String name;

    private String description;

    private String color;

    @NotNull(message = "Privacy setting is required")
    private Boolean isPrivate;

    private String packageId;

    private String subjectId;

    private String slotId;
}
