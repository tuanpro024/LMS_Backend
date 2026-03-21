package com.lms.onllearning.dto.request;

import com.lms.onllearning.entity.enums.ScheduleModuleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request để import Excel tạo module ôn luyện mới
 * rồi gắn vào một buổi học (syllabus_schedule).
 * File được truyền riêng qua @RequestPart("file").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportModuleToScheduleRequest {

    @NotBlank(message = "schedule ID là bắt buộc")
    @Size(max = 26)
    private String scheduleId;

    @NotNull(message = "Loại module là bắt buộc")
    private ScheduleModuleType moduleType;

    @NotNull(message = "Thứ tự module là bắt buộc")
    @Min(value = 1, message = "Thứ tự module phải >= 1")
    private Integer moduleOrder;

    @NotBlank(message = "Tên module là bắt buộc")
    @Size(max = 255)
    private String title;

    private String description;

    private Boolean isRequired;
}
