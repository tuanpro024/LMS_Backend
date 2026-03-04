package com.lms.videocourse.service;

import com.lms.videocourse.dto.request.CreateVideoModuleRequest;
import com.lms.videocourse.dto.request.UpdateVideoModuleRequest;
import com.lms.videocourse.dto.response.VideoModuleResponse;

import java.util.List;

public interface IVideoModuleService {

    VideoModuleResponse createVideoModule(CreateVideoModuleRequest request);

    VideoModuleResponse getVideoModuleById(String id, String userId);

    List<VideoModuleResponse> getVideoModulesByStepId(String stepId, String userId);

    VideoModuleResponse updateVideoModule(String id, UpdateVideoModuleRequest request);

    void deleteVideoModule(String id);
}
