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

import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importTeachers(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "File không được để trống");
        }

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<User> teachersToSave = new ArrayList<>();
            Role teacherRole = roleRepository.findByName(RoleName.ROLE_TEACHER)
                    .orElseThrow(() -> new ApiException(ErrorCode.E221, "Role ROLE_TEACHER not found"));

            // Assume row 0 is header
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String email = getCellValueAsString(row.getCell(0));
                String password = getCellValueAsString(row.getCell(1));
                String fullName = getCellValueAsString(row.getCell(2));
                String phoneNumber = getCellValueAsString(row.getCell(3));
                String shortDescription = getCellValueAsString(row.getCell(4));
                String fullDescription = getCellValueAsString(row.getCell(5));
                String teachingStyle = getCellValueAsString(row.getCell(6));
                String qualification = getCellValueAsString(row.getCell(7));
                String videoIntroLink = getCellValueAsString(row.getCell(8));
                String avatarUrl = getCellValueAsString(row.getCell(9));

                if (email.isEmpty() && password.isEmpty() && fullName.isEmpty()) {
                    continue; // skip empty rows
                }

                if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                    throw new ApiException(ErrorCode.BAD_REQUEST, "Dòng " + (i + 1) + ": Thiếu thông tin bắt buộc (Email, Mật khẩu, Họ và tên)");
                }

                if (userRepository.existsByEmail(email)) {
                    throw new ApiException(ErrorCode.CONFLICT, "Dòng " + (i + 1) + ": Email " + email + " đã được sử dụng");
                }

                User user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .fullName(fullName)
                        .phoneNumber(phoneNumber)
                        .shortDescription(shortDescription.isEmpty() ? null : shortDescription)
                        .fullDescription(fullDescription.isEmpty() ? null : fullDescription)
                        .teachingStyle(teachingStyle.isEmpty() ? null : teachingStyle)
                        .qualification(qualification.isEmpty() ? null : qualification)
                        .videoIntroLink(videoIntroLink.isEmpty() ? null : videoIntroLink)
                        .avatarUrl(avatarUrl.isEmpty() ? null : avatarUrl)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .roles(Set.of(teacherRole))
                        .build();

                teachersToSave.add(user);
            }

            userRepository.saveAll(teachersToSave);
            return teachersToSave.size();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Lỗi khi xử lý file Excel: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                long num = (long) cell.getNumericCellValue();
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
