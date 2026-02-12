package com.lms.content.common.dto.response;

import com.lms.content.common.entity.TypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeResponse {

    private String id;
    private TypeName name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
