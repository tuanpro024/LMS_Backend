package com.lms.writing.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.writing.dto.request.CreateSubjectRequest;
import com.lms.writing.dto.request.UpdateSubjectRequest;
import com.lms.writing.dto.response.SubjectResponse;
import com.lms.writing.entity.Package;
import com.lms.writing.entity.Subject;
import com.lms.writing.mapper.SubjectMapper;
import com.lms.writing.repository.PackageRepository;
import com.lms.writing.repository.SubjectRepository;
import com.lms.writing.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final PackageRepository packageRepository;
    private final SubjectMapper subjectMapper;

    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request, String userId) {
        log.info("Creating subject for user: {}", userId);

        Package packageEntity = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        // Check ownership
        if (!packageEntity.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add subject to this package");
        }

        Subject subject = subjectMapper.toEntity(request);
        subject.setUserId(userId);
        subject.setPackageEntity(packageEntity);

        Subject saved = subjectRepository.save(subject);
        return subjectMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(String id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));
        return subjectMapper.toResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjectsByPackageId(String packageId) {
        List<Subject> subjects = subjectRepository.findByPackageEntityId(packageId);
        return subjectMapper.toResponseList(subjects);
    }

    @Override
    public SubjectResponse updateSubject(String id, UpdateSubjectRequest request, String userId) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

        // Check ownership
        if (!subject.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this subject");
        }

        subjectMapper.updateEntity(subject, request);

        Subject updated = subjectRepository.save(subject);
        return subjectMapper.toResponse(updated);
    }

    @Override
    public void deleteSubject(String id, String userId) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

        // Check ownership
        if (!subject.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this subject");
        }

        subjectRepository.delete(subject);
    }
}
