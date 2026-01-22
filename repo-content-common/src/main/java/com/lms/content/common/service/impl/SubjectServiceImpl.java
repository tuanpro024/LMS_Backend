package com.lms.content.common.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.dto.request.CreateSubjectRequest;
import com.lms.content.common.dto.request.UpdateSubjectRequest;
import com.lms.content.common.dto.response.SubjectResponse;
import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.Subject;
import com.lms.content.common.mapper.SubjectMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.SubjectRepository;
import com.lms.content.common.service.SubjectService;
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
    private final FolderRepository folderRepository;
    private final SubjectMapper subjectMapper;

    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request, String userId) {
        log.info("Creating subject for user: {} with code: {}", userId, request.getCode());

        // Hook: validate before processing
        validateCreateSubject(request, userId);

        // Verify package exists and user has access
        Package packageEntity = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        if (!packageEntity.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add subject to this package");
        }

        Subject subject = subjectMapper.toEntity(request);
        subject.setUserId(userId);
        subject.setPackageEntity(packageEntity);

        // Hook: before save
        beforeSaveSubject(subject, request);

        Subject saved = subjectRepository.save(subject);

        // Hook: after save
        afterSaveSubject(saved);

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
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        return subjectMapper.toResponseList(subjects);
    }

    @Override
    public SubjectResponse updateSubject(String id, UpdateSubjectRequest request, String userId) {
        Subject subject = subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

        // Hook: validate before update
        validateUpdateSubject(subject, request, userId);

        // Update fields if provided
        if (request.getName() != null) {
            subject.setName(request.getName());
        }
        if (request.getCode() != null) {
            subject.setCode(request.getCode());
        }
        if (request.getDescription() != null) {
            subject.setDescription(request.getDescription());
        }

        // Hook: before update
        beforeUpdateSubject(subject, request);

        Subject updated = subjectRepository.save(subject);

        // Hook: after update
        afterUpdateSubject(updated);

        return subjectMapper.toResponse(updated);
    }

    @Override
    public void deleteSubject(String id, String userId) {
        Subject subject = subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

        // Hook: before delete
        beforeDeleteSubject(subject, userId);

        // Cascade delete will handle slots automatically due to orphanRemoval = true
        subjectRepository.delete(subject);

        // Hook: after delete
        afterDeleteSubject(id, userId);
    }

    @Override
    public SubjectResponse addFolderToSubject(String subjectId, String folderId, String userId) {
        Subject subject = subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership of folder
        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add this folder");
        }

        subject.addFolder(folder);
        Subject updated = subjectRepository.save(subject);

        return subjectMapper.toResponse(updated);
    }

    @Override
    public SubjectResponse removeFolderFromSubject(String subjectId, String folderId, String userId) {
        Subject subject = subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        subject.removeFolder(folder);
        Subject updated = subjectRepository.save(subject);

        return subjectMapper.toResponse(updated);
    }

    // ========== EXTENSION HOOKS (protected, non-final) ==========

    protected void validateCreateSubject(CreateSubjectRequest request, String userId) {
        // Default implementation - empty
    }

    protected void beforeSaveSubject(Subject subject, CreateSubjectRequest request) {
        // Default implementation - empty
    }

    protected void afterSaveSubject(Subject saved) {
        // Default implementation - empty
    }

    protected void validateUpdateSubject(Subject entity, UpdateSubjectRequest request, String userId) {
        // Default implementation - empty
    }

    protected void beforeUpdateSubject(Subject entity, UpdateSubjectRequest request) {
        // Default implementation - empty
    }

    protected void afterUpdateSubject(Subject updated) {
        // Default implementation - empty
    }

    protected void beforeDeleteSubject(Subject entity, String userId) {
        // Default implementation - empty
    }

    protected void afterDeleteSubject(String subjectId, String userId) {
        // Default implementation - empty
    }
}
