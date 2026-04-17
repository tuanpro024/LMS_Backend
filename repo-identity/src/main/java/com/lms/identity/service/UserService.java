package com.lms.identity.service;

import com.lms.common.dto.PageResponse;
import com.lms.identity.dto.response.ProfileResponse;
import com.lms.identity.dto.request.UpdateProfileRequest;
import com.lms.identity.dto.request.ChangePasswordRequest;

public interface UserService {
    ProfileResponse getProfile(String userId);

    ProfileResponse updateProfile(String userId, UpdateProfileRequest request);

    void changePassword(String userId, ChangePasswordRequest request);

    void updatePremiumStatus(String userId, boolean isPremium, int durationInDays);

    boolean isPremium(String userId);

    java.util.List<ProfileResponse> getProfilesByIds(java.util.List<String> userIds);

    PageResponse<String> getActiveUserIds(int page, int size);
}
