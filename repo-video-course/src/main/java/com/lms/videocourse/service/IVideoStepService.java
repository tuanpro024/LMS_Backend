package com.lms.videocourse.service;

import com.lms.videocourse.dto.request.CreateVideoStepRequest;
import com.lms.videocourse.dto.request.UpdateVideoStepRequest;
import com.lms.videocourse.dto.response.VideoStepResponse;

import java.util.List;

public interface IVideoStepService {

    VideoStepResponse createVideoStep(CreateVideoStepRequest request);

    VideoStepResponse getVideoStepById(String id);

    List<VideoStepResponse> getVideoStepsByCourseId(String studySetId, String userId);

    VideoStepResponse updateVideoStep(String id, UpdateVideoStepRequest request);

    void deleteVideoStep(String id);
}
