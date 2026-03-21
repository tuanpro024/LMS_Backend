package com.lms.onllearning.service;

import com.lms.onllearning.dto.request.AddModuleToScheduleRequest;
import com.lms.onllearning.dto.request.ImportModuleToScheduleRequest;
import com.lms.onllearning.dto.request.ReorderScheduleModulesRequest;
import com.lms.onllearning.dto.response.ScheduleModuleResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Dịch vụ quản lý module ôn luyện trong từng buổi học (SyllabusSchedule).
 */
public interface IScheduleModuleService {

    /** Thêm module đã có (chọn study set có sẵn từ repo ngoài). */
    ScheduleModuleResponse addModule(AddModuleToScheduleRequest request);

    /** Import Excel tạo content mới → lấy studySetId → lưu module. */
    ScheduleModuleResponse importModule(ImportModuleToScheduleRequest request, MultipartFile file);

    /** Lấy một module theo ID. */
    ScheduleModuleResponse getById(String moduleId);

    /** Lấy tất cả module active của một buổi học (hiển thị cho học sinh). */
    List<ScheduleModuleResponse> getByScheduleId(String scheduleId);

    /** Soft delete module. */
    void removeModule(String moduleId);

    /** Sắp xếp lại thứ tự các module trong buổi học. */
    void reorderModules(String scheduleId, ReorderScheduleModulesRequest request);
}
