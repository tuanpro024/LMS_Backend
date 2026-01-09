package com.lms.identity.service;

import com.lms.identity.dto.request.AdminCreateUserRequest;
import com.lms.identity.dto.request.AdminUpdateUserRequest;
import com.lms.identity.dto.request.AdminUserFilterRequest;
import com.lms.identity.dto.response.AdminUserPageResponse;
import com.lms.identity.dto.response.AdminUserResponse;

public interface AdminService {
    AdminUserPageResponse listUsers(AdminUserFilterRequest filter);
    AdminUserResponse getUser(String userId);
    AdminUserResponse createUser(AdminCreateUserRequest request);
    AdminUserResponse updateUser(String userId, AdminUpdateUserRequest request);
    void deleteUser(String userId);
    AdminUserResponse activateUser(String userId);
    AdminUserResponse blockUser(String userId);
}
