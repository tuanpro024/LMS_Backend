package com.lms.identity.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.identity.dto.response.ProfileResponse;
import com.lms.identity.dto.request.UpdateProfileRequest;
import com.lms.identity.dto.request.ChangePasswordRequest;
import com.lms.identity.entity.User;
import com.lms.identity.mapper.UserMapper;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import com.lms.identity.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional(readOnly = true)
    @Override
    public ProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        return userMapper.toProfile(user);
    }

    @Transactional
    @Override
    public ProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        User saved = userRepository.save(user);

        // Phát sự kiện Kafka Commit nếu avatar là URL từ file service của chúng ta
        if (request.getAvatarUrl() != null && request.getAvatarUrl().contains("/api/media/file/")) {
            String fileId = request.getAvatarUrl().substring(request.getAvatarUrl().lastIndexOf('/') + 1);
            String jsonPayload = "[\"" + fileId + "\"]";
            kafkaTemplate.send("storage.file.commit", jsonPayload);
        }

        return userMapper.toProfile(saved);
    }

    @Transactional
    @Override
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        // Users who signed up via Google/OAuth may not have a local password
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ApiException(ErrorCode.E204, "This account uses social login and has no local password");
        }

        // Verify current password – guard against corrupted hash
        boolean currentMatch;
        try {
            currentMatch = passwordEncoder.matches(request.getCurrentPassword(), user.getPassword());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.E204, "Current password is incorrect");
        }
        if (!currentMatch) {
            throw new ApiException(ErrorCode.E204, "Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException(ErrorCode.E204, "New password and confirm password do not match");
        }

        // Check new != current
        try {
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new ApiException(ErrorCode.E204, "New password must be different from the current password");
            }
        } catch (ApiException ae) {
            throw ae;
        } catch (Exception ignored) {
            // corrupted hash – allow change
        }

        // Validate password complexity manually (removed @Pattern to avoid regex
        // issues)
        String pwd = request.getNewPassword();
        if (!pwd.matches(".*[0-9].*") || !pwd.matches(".*[a-z].*")
                || !pwd.matches(".*[A-Z].*") || !pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new ApiException(ErrorCode.E252,
                    "Password must contain at least one digit, one lowercase letter, one uppercase letter and one special character");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
