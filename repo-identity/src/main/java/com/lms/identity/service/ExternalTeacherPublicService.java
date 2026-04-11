package com.lms.identity.service;

import com.lms.identity.dto.response.ExternalTeacherPublicResponse;

import java.util.List;

public interface ExternalTeacherPublicService {
    List<ExternalTeacherPublicResponse> getAllTeachers();

    ExternalTeacherPublicResponse getTeacherById(String id);
}
