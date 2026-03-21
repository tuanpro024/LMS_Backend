package com.lms.onllearning.dto.response;

import com.lms.onllearning.entity.enums.ScheduleModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response trả về thông tin một module ôn luyện trong buổi học.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleModuleResponse {

    private String id;
    private String scheduleId;
    private ScheduleModuleType moduleType;
    private Integer moduleOrder;
    private String title;
    private String description;
    private String contentSetId;
    private String contentFolderId;
    private String externalRefJson;
    private Boolean isRequired;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
