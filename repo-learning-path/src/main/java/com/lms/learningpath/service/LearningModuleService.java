package com.lms.learningpath.service;

import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.CreateModuleRequest;
import com.lms.learningpath.dto.request.UpdateModuleRequest;
import com.lms.learningpath.dto.response.ModuleCompleteResponse;
import com.lms.learningpath.dto.response.ModuleResponse;

import java.util.List;

public interface LearningModuleService {

    /**
     * Tạo module mới cho study set
     */
    ModuleResponse createModule(CreateModuleRequest request, String userId);

    /**
     * Lấy thông tin module theo ID
     */
    ModuleResponse getModuleById(String id);

    /**
     * Lấy danh sách modules của study set (có progress của user)
     */
    List<ModuleResponse> getModulesByStudySetId(String studySetId, String userId);

    /**
     * Cập nhật thông tin module
     */
    ModuleResponse updateModule(String id, UpdateModuleRequest request, String userId);

    /**
     * Xóa module
     */
    void deleteModule(String id, String userId);

    /**
     * Bắt đầu học module
     */
    ModuleCompleteResponse startModule(String moduleId, String userId);

    /**
     * Hoàn thành module
     */
    ModuleCompleteResponse completeModule(String moduleId, String userId, CompleteModuleRequest request);
}