package com.lms.content.common.service.impl;

import com.lms.common.event.PackageStatusEvent;
import com.lms.common.event.PackageStatusPublisher;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.dto.request.CreateStudySetRequest;
import com.lms.content.common.dto.request.UpdateStudySetRequest;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.entity.enums.PublishStatus;
import com.lms.content.common.mapper.StudySetMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.content.common.service.StudySetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudySetServiceImpl implements StudySetService {

    private final StudySetRepository studySetRepository;
    private final FolderRepository folderRepository;
    private final PackageRepository packageRepository;
    private final StudySetMapper studySetMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PackageStatusPublisher packageStatusPublisher;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Override
    public StudySetResponse createStudySet(CreateStudySetRequest request, String userId) {
        log.info("Creating study set for user: {}", userId);

        validateCreateStudySet(request, userId);

        StudySet studySet = studySetMapper.toEntity(request);
        studySet.setUserId(userId);

        beforeSaveStudySet(studySet, request);

        StudySet saved = studySetRepository.save(studySet);

        // Link to folder if provided
        if (request.getFolderId() != null && !request.getFolderId().trim().isEmpty()) {
            Folder folder = folderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

            if (!folder.getUserId().equals(userId)) {
                throw new ApiException(ErrorCode.E240, "No permission to add study set to this folder");
            }

            folder.addStudySet(saved);
            Folder savedFolder = folderRepository.save(folder);

            // StudySet mới trong folder → revert package về DRAFT
            revertParentPackagesToDraft(savedFolder, userId);
        }

        afterSaveStudySet(saved);

        return studySetMapper.toResponse(saved);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StudySetResponse getStudySetById(String id) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));
        return studySetMapper.toResponse(studySet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getStudySetsByUserId(String userId) {
        List<StudySet> studySets = studySetRepository.findByUserId(userId);
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getStudySetsByFolderId(String folderId) {
        List<StudySet> studySets = studySetRepository.findByFolderId(folderId);
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> searchStudySets(String keyword) {
        List<StudySet> studySets = studySetRepository.searchByKeyword(keyword);
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getAllStudySets() {
        List<StudySet> studySets = studySetRepository.findAll();
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getStudySetsByPackageType(String packageType) {
        try {
            com.lms.content.common.entity.TypeName typeName =
                    com.lms.content.common.entity.TypeName.valueOf(packageType);
            List<StudySet> studySets = typeName == com.lms.content.common.entity.TypeName.VIDEO_COURSE
                    ? studySetRepository.findByPackageTypeNameStrict(typeName)
                    : studySetRepository.findByPackageTypeName(typeName);
            return studySetMapper.toResponseList(studySets);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid packageType provided: {}", packageType);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> findByTitleAndUserIdIgnoreCase(String title, String userId) {
        List<StudySet> studySets = studySetRepository.findByTitleAndUserIdIgnoreCase(title, userId);
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudySetLearningAllowed(String studySetId) {
        if (studySetId == null || studySetId.isBlank()) {
            return false;
        }

        if (!studySetRepository.existsById(studySetId)) {
            return false;
        }

        long linkedFolderCount = studySetRepository.countActiveLinkedFolders(studySetId);
        if (linkedFolderCount == 0) {
            // Backward compatible: orphan study sets are still learnable.
            return true;
        }

        return studySetRepository.existsInPublishedPackage(studySetId);
    }

    @Override
    public void assertStudySetLearningAllowed(String studySetId) {
        if (!isStudySetLearningAllowed(studySetId)) {
            throw new ApiException(
                    ErrorCode.FORBIDDEN,
                    "Study set is unavailable for learning because its package is DRAFT");
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Override
    public StudySetResponse updateStudySet(String id, UpdateStudySetRequest request, String userId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        validateUpdateStudySet(studySet, request, userId);

        if (request.getTitle() != null) {
            studySet.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            studySet.setDescription(request.getDescription());
        }
        if (request.getIsPrivate() != null) {
            studySet.setPrivate(request.getIsPrivate());
        }

        beforeUpdateStudySet(studySet, request);

        StudySet updated = studySetRepository.save(studySet);

        // StudySet bị sửa → revert các package cha về DRAFT
        revertParentPackagesOfStudySetToDraft(updated, userId);

        afterUpdateStudySet(updated);

        return studySetMapper.toResponse(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Override
    public void deleteStudySet(String id, String userId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        // Lưu danh sách folder trước khi xóa
        List<Folder> parentFolders = studySet.getFolders() != null
                ? List.copyOf(studySet.getFolders())
                : List.of();

        // Unlink from all folders
        if (!parentFolders.isEmpty()) {
            for (Folder folder : parentFolders) {
                folder.removeStudySet(studySet);
                folderRepository.save(folder);
            }
        }

        beforeDeleteStudySet(studySet, userId);

        eventPublisher.publishEvent(
                new com.lms.content.common.event.StudySetDeletedEvent(this, id));

        studySetRepository.delete(studySet);

        // StudySet bị xóa → revert package cha về DRAFT
        for (Folder folder : parentFolders) {
            if (folder.getPackageEntity() != null) {
                packageRepository.findById(folder.getPackageEntity().getId())
                        .ifPresent(pkg -> revertPackageToDraft(pkg, userId));
            }
        }

        afterDeleteStudySet(id, userId);
    }

    // ── REVERT HELPERS ────────────────────────────────────────────────────────

    /**
     * Revert package cha của folder về DRAFT nếu đang PUBLISHED.
     * Tránh circular dependency — dùng PackageRepository trực tiếp.
     */
    private void revertParentPackagesToDraft(Folder folder, String userId) {
        if (folder.getPackageEntity() != null) {
            packageRepository.findById(folder.getPackageEntity().getId())
                    .ifPresent(pkg -> revertPackageToDraft(pkg, userId));
        }
    }

    /**
     * Revert tất cả package cha của studySet về DRAFT nếu đang PUBLISHED.
     */
    private void revertParentPackagesOfStudySetToDraft(StudySet studySet, String userId) {
        if (studySet.getFolders() == null || studySet.getFolders().isEmpty()) return;

        studySet.getFolders().stream()
                .filter(f -> f.getPackageEntity() != null)
                .map(f -> f.getPackageEntity().getId())
                .distinct()
                .forEach(pkgId -> packageRepository.findById(pkgId)
                        .ifPresent(pkg -> revertPackageToDraft(pkg, userId)));
    }

    /**
     * Nếu package đang PUBLISHED → set DRAFT + broadcast Kafka event.
     * Idempotent: nếu đã DRAFT thì bỏ qua.
     */
    private void revertPackageToDraft(Package pkg, String triggeredBy) {
        if (pkg.getPublishStatus() == PublishStatus.PUBLISHED) {
            pkg.setPublishStatus(PublishStatus.DRAFT);
            packageRepository.save(pkg);

            packageStatusPublisher.publish(new PackageStatusEvent(
                    pkg.getId(),
                    pkg.getName(),
                    pkg.getType() != null ? pkg.getType().getName().name() : null,
                    PublishStatus.DRAFT.name(),
                    triggeredBy,
                    "CONTENT_UPDATED"
            ));

            log.info("Package {} auto-reverted to DRAFT due to study set CUD by userId={}",
                    pkg.getId(), triggeredBy);
        }
    }

    // ── EXTENSION HOOKS ───────────────────────────────────────────────────────

    protected void validateCreateStudySet(CreateStudySetRequest request, String userId) {
    }

    protected void beforeSaveStudySet(StudySet studySet, CreateStudySetRequest request) {
    }

    protected void afterSaveStudySet(StudySet saved) {
    }

    protected void validateUpdateStudySet(StudySet entity, UpdateStudySetRequest request, String userId) {
    }

    protected void beforeUpdateStudySet(StudySet entity, UpdateStudySetRequest request) {
    }

    protected void afterUpdateStudySet(StudySet updated) {
    }

    protected void beforeDeleteStudySet(StudySet entity, String userId) {
    }

    protected void afterDeleteStudySet(String studySetId, String userId) {
    }
}
