package com.lms.identity.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.identity.dto.response.ExternalTeacherPublicResponse;
import com.lms.identity.entity.ExternalTeacher;
import com.lms.identity.repository.ExternalTeacherRepository;
import com.lms.identity.service.ExternalTeacherPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalTeacherPublicServiceImpl implements ExternalTeacherPublicService {

    private final ExternalTeacherRepository externalTeacherRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ExternalTeacherPublicResponse> getAllTeachers() {
        return externalTeacherRepository.findAllActiveOrderByRatingDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExternalTeacherPublicResponse getTeacherById(String id) {
        ExternalTeacher teacher = externalTeacherRepository.findActiveById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Teacher not found"));

        return toResponse(teacher);
    }

    private ExternalTeacherPublicResponse toResponse(ExternalTeacher teacher) {
        return new ExternalTeacherPublicResponse(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getAvatarUrl(),
                teacher.getFullDescription(),
                teacher.getQualification(),
                teacher.getRating(),
                teacher.getShortDescription(),
                teacher.getTeachingStyle(),
                teacher.getVideoIntroLink()
        );
    }
}
