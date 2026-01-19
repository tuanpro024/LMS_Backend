package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.CreateSubjectRequest;
import com.lms.flashcard.dto.request.UpdateSubjectRequest;
import com.lms.flashcard.dto.response.SubjectResponse;
import com.lms.flashcard.entity.Folder;
import com.lms.flashcard.entity.Package;
import com.lms.flashcard.entity.Subject;
import com.lms.flashcard.mapper.SubjectMapper;
import com.lms.flashcard.repository.FolderRepository;
import com.lms.flashcard.repository.PackageRepository;
import com.lms.flashcard.repository.SubjectRepository;
import com.lms.flashcard.service.SubjectService;
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

        // Verify package exists and user has access
        Package packageEntity = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

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
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        return subjectMapper.toResponseList(subjects);
    }

    @Override
    public SubjectResponse updateSubject(String id, UpdateSubjectRequest request, String userId) {
        Subject subject = subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

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

        Subject updated = subjectRepository.save(subject);
        return subjectMapper.toResponse(updated);
    }

    @Override
    public void deleteSubject(String id, String userId) {
        Subject subject = subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

        // Cascade delete will handle slots automatically due to orphanRemoval = true
        subjectRepository.delete(subject);
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
}
