package com.lms.identity.service;

import com.lms.identity.dto.response.ProfileResponse;
import com.lms.identity.dto.request.UpdateProfileRequest;
import com.lms.identity.dto.request.ChangePasswordRequest;

public interface UserService {
    ProfileResponse getProfile(String userId);

    ProfileResponse updateProfile(String userId, UpdateProfileRequest request);

    void changePassword(String userId, ChangePasswordRequest request);
}
