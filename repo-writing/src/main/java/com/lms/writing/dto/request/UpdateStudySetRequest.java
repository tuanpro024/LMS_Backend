package com.lms.writing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStudySetRequest {
    private String title;
    private String description;
    private Boolean isPrivate;
}
