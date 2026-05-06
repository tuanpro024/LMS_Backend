package com.lms.identity.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.identity.dto.request.AdminCreateUserRequest;
import com.lms.identity.dto.request.AdminUpdateUserRequest;
import com.lms.identity.dto.response.AdminUserResponse;
import com.lms.identity.entity.Role;
import com.lms.identity.entity.RoleName;
import com.lms.identity.entity.User;
import com.lms.identity.entity.UserStatus;
import com.lms.identity.mapper.UserMapper;
import com.lms.identity.repository.RoleRepository;
import com.lms.identity.repository.UserRepository;
import com.lms.identity.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllTeachers() {
        return userRepository.findByRolesName(RoleName.ROLE_TEACHER).stream()
                .map(userMapper::toAdmin)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserResponse createTeacher(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.CONFLICT, "Email already in use");
        }

        Role teacherRole = roleRepository.findByName(RoleName.ROLE_TEACHER)
                .orElseThrow(() -> new ApiException(ErrorCode.E221, "Role ROLE_TEACHER not found"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .avatarUrl(request.getAvatarUrl())
                .address(request.getAddress())
                .gender(request.getGender())
                .dob(request.getDob())
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(Set.of(teacherRole))
                // Optional teacher profile fields
                .shortDescription(request.getShortDescription())
                .fullDescription(request.getFullDescription())
                .teachingStyle(request.getTeachingStyle())
                .qualification(request.getQualification())
                .videoIntroLink(request.getVideoIntroLink())
                .build();

        User saved = userRepository.save(user);
        return userMapper.toAdmin(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse updateTeacher(String id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Teacher not found"));

        // Ensure user is actually a teacher
        boolean isTeacher = user.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.ROLE_TEACHER);
        if (!isTeacher) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "User is not a teacher");
        }

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null)
            user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAvatarUrl() != null)
            user.setAvatarUrl(request.getAvatarUrl());
        if (request.getAddress() != null)
            user.setAddress(request.getAddress());
        if (request.getGender() != null)
            user.setGender(request.getGender());
        if (request.getDob() != null)
            user.setDob(request.getDob());
        if (request.getStatus() != null)
            user.setStatus(request.getStatus());
        // Optional teacher profile fields (null means "not supplied" – leave unchanged)
        if (request.getShortDescription() != null)
            user.setShortDescription(request.getShortDescription());
        if (request.getFullDescription() != null)
            user.setFullDescription(request.getFullDescription());
        if (request.getTeachingStyle() != null)
            user.setTeachingStyle(request.getTeachingStyle());
        if (request.getQualification() != null)
            user.setQualification(request.getQualification());
        if (request.getVideoIntroLink() != null)
            user.setVideoIntroLink(request.getVideoIntroLink());

        User saved = userRepository.save(user);
        return userMapper.toAdmin(saved);
    }

    @Override
    @Transactional
    public void blockTeacher(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Teacher not found"));
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
    }
}
