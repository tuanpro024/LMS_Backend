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
public class CreateSubjectRequest {

    @NotNull(message = "Package ID is required")
    private String packageId;

    @NotBlank(message = "Subject name is required")
    private String name;

    @NotBlank(message = "Subject code is required")
    private String code;

    private String description;
}
