package com.lms.content.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudySetRequest {

    private String title;

    private String description;

    private String thumbnail;

    private String unlockRuleJson;

    private Integer estimatedMinutes;

    private Boolean isPrivate;
}
