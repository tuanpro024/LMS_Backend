package com.lms.onllearning.service;

import com.lms.onllearning.dto.response.AvailableScheduleModuleResponse;
import com.lms.onllearning.entity.enums.ScheduleModuleType;

import java.util.List;

/**
 * Dịch vụ lấy danh sách study set có sẵn từ các repo ôn luyện.
 * Dùng khi ADMIN/TEACHER_MANAGER chọn module để gắn vào buổi học.
 */
public interface IAvailableScheduleModuleService {

    List<AvailableScheduleModuleResponse> getAllAvailableModules(String query);

    List<AvailableScheduleModuleResponse> getAvailableModulesByType(ScheduleModuleType type, String query);
}
