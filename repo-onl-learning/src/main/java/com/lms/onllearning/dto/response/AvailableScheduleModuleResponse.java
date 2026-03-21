package com.lms.onllearning.dto.response;

import com.lms.onllearning.entity.enums.ScheduleModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response khi browse các study set có sẵn từ các repo ôn luyện.
 * Dùng khi ADMIN/TEACHER_MANAGER chọn module để thêm vào buổi học.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableScheduleModuleResponse {

    private String id;           // StudySet ID từ repo ngoài
    private String title;
    private String description;
    private String thumbnail;
    private ScheduleModuleType moduleType;
    private String repoName;     // "repo-flashcard", "repo-writing", ...
    private Long itemCount;
    private Boolean isPrivate;
    private String userId;       // Owner
}
