package com.lms.onllearning.service;

import com.lms.onllearning.dto.response.OnlineCourseResponse;
import com.lms.onllearning.dto.request.OnlineCourseRequest;
import com.lms.onllearning.dto.response.OnlineCourseFullDetailResponse;

import java.util.List;

public interface IOnlineCourseService {
    List<OnlineCourseResponse> getAll();
    long countActiveCourses();
    OnlineCourseResponse getById(String id);
    OnlineCourseFullDetailResponse getFullDetail(String id);
    OnlineCourseResponse create(OnlineCourseRequest request);
    OnlineCourseResponse update(String id, OnlineCourseRequest request);
    void delete(String id);
}
