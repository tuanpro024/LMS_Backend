package com.lms.content.common.dto.request;

import com.lms.content.common.entity.TypeName;
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

    private TypeName type;

    private String description;
}
