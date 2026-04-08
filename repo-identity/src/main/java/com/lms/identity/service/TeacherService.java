package com.lms.identity.service;

import com.lms.identity.dto.request.AdminCreateUserRequest;
import com.lms.identity.dto.request.AdminUpdateUserRequest;
import com.lms.identity.dto.response.AdminUserResponse;

import java.util.List;

public interface TeacherService {
    List<AdminUserResponse> getAllTeachers();
    AdminUserResponse createTeacher(AdminCreateUserRequest request);
    AdminUserResponse updateTeacher(String id, AdminUpdateUserRequest request);
    void blockTeacher(String id);
}
