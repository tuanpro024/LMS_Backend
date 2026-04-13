package com.lms.identity.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.identity.dto.request.AdminCreateUserRequest;
import com.lms.identity.dto.request.AdminUpdateUserRequest;
import com.lms.identity.dto.request.AdminUserFilterRequest;
import com.lms.identity.dto.response.AdminUserPageResponse;
import com.lms.identity.dto.response.AdminUserResponse;
import com.lms.identity.entity.Role;
import com.lms.identity.entity.RoleName;
import com.lms.identity.entity.User;
import com.lms.identity.entity.UserStatus;
import com.lms.identity.mapper.UserMapper;
import com.lms.identity.repository.RoleRepository;
import com.lms.identity.repository.UserRepository;
import com.lms.identity.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import com.lms.identity.service.AdminService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public AdminUserPageResponse listUsers(AdminUserFilterRequest filter) {
        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize());
        Specification<User> spec = UserSpecification.buildSpecification(filter);

        Page<User> userPage = userRepository.findAll(spec, pageRequest);

        List<AdminUserResponse> items = userPage.getContent().stream()
                .map(userMapper::toAdmin)
                .toList();

        return AdminUserPageResponse.builder()
                .items(items)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public AdminUserResponse getUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        return userMapper.toAdmin(user);
    }

    @Transactional
    @Override
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.CONFLICT, "Email already in use");
        }
        Set<Role> roles = resolveRoles(request.getRoles());
        if (roles.isEmpty()) {
            Role defaultRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new ApiException(ErrorCode.E221, "Role not found data: ROLE_USER"));
            roles.add(defaultRole);
        }
        UserStatus status = request.getStatus() == null ? UserStatus.ACTIVE : request.getStatus();
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .avatarUrl(request.getAvatarUrl())
                .address(request.getAddress())
                .gender(request.getGender())
                .dob(request.getDob())
                .status(status)
                .emailVerified(true)
                .roles(roles)
                .build();
        User saved = userRepository.save(user);
        return userMapper.toAdmin(saved);
    }

    @Transactional
    @Override
    public AdminUserResponse updateUser(String userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoles()));
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }

        User saved = userRepository.save(user);
        return userMapper.toAdmin(saved);
    }

    @Transactional
    @Override
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    @Override
    public AdminUserResponse activateUser(String userId) {
        return updateStatus(userId, UserStatus.ACTIVE);
    }

    @Transactional
    @Override
    public AdminUserResponse blockUser(String userId) {
        return updateStatus(userId, UserStatus.BLOCKED);
    }

    private AdminUserResponse updateStatus(String userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        user.setStatus(status);
        User saved = userRepository.save(user);
        return userMapper.toAdmin(saved);
    }

    private Set<Role> resolveRoles(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return new HashSet<>();
        }

        return names.stream()
                .map(name -> {
                    if (!isValidRoleName(name)) {
                        throw new ApiException(
                                ErrorCode.E221,
                                "Role not found data: " + name);
                    }

                    return roleRepository.findByName(RoleName.valueOf(name))
                            .orElseThrow(() -> new ApiException(
                                    ErrorCode.E221,
                                    "Role not found data: " + name));
                })
                .collect(Collectors.toSet());
    }

    private boolean isValidRoleName(String name) {
        try {
            RoleName.valueOf(name);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
