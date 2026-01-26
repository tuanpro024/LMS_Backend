package com.lms.content.common.dto.request;

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
public class CreateFolderRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "isPrivate is required")
    private Boolean isPrivate;

    private String packageId;

    private String subjectId;

    private String slotId;
}
