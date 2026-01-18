package com.lms.writing.dto.request;

import com.lms.writing.entity.TypeName;
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
public class CreatePackageRequest {

    @NotBlank(message = "Package name is required")
    private String name;

    @NotNull(message = "Type is required")
    private TypeName type;

    private String description;
}
