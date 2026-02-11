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
public class CreateStudySetRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String thumbnail;

    @NotNull(message = "isPrivate is required")
    private Boolean isPrivate;

    private String folderId;
}
