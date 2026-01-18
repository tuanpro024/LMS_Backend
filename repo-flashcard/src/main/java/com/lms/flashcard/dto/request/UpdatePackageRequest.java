package com.lms.flashcard.dto.request;

import com.lms.flashcard.entity.TypeName;
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
