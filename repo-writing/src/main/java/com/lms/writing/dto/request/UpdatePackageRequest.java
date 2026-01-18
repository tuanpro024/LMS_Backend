package com.lms.writing.dto.request;

import com.lms.writing.entity.TypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePackageRequest {
    private String name;
    private TypeName type;
    private String description;
}
