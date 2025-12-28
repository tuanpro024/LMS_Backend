package com.lms.identity.service;

import com.lms.identity.dto.response.ProfileResponse;
import com.lms.identity.dto.request.UpdateProfileRequest;

public interface UserService {
    ProfileResponse getProfile(String userId);
    ProfileResponse updateProfile(String userId, UpdateProfileRequest request);
}
