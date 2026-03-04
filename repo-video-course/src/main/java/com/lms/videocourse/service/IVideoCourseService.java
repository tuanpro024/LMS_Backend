package com.lms.videocourse.service;

import com.lms.videocourse.dto.request.CreateVideoCourseRequest;
import com.lms.videocourse.dto.request.UpdateVideoCourseRequest;
import com.lms.videocourse.dto.response.VideoCourseResponse;

import java.util.List;

public interface IVideoCourseService {

    VideoCourseResponse createVideoCourse(CreateVideoCourseRequest request, String createdBy);

    VideoCourseResponse getVideoCourseById(String id);

    List<VideoCourseResponse> getVideoCoursesByStudySetId(String studySetId);

    VideoCourseResponse updateVideoCourse(String id, UpdateVideoCourseRequest request, String updatedBy);

    void deleteVideoCourse(String id);
}
