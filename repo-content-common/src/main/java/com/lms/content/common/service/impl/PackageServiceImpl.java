package com.lms.content.common.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.http.TicketAccessClient;
import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.Type;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.entity.CategoryType;
import com.lms.content.common.entity.enums.PublishStatus;
import com.lms.content.common.mapper.PackageMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.TypeRepository;
import com.lms.content.common.service.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PackageServiceImpl implements PackageService {

    private static final Set<String> PRIVILEGED_ROLES = Set.of("ROLE_ADMIN", "ROLE_TEACHER_MANAGER");

    private final PackageRepository packageRepository;
    private final FolderRepository folderRepository;
    private final PackageMapper packageMapper;
    private final TypeRepository typeRepository;
    private final com.lms.content.common.service.FolderService folderService;
    private final StudySetDeletionService studySetDeletionService;
    private final TicketAccessClient ticketAccessClient;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Override
    public PackageResponse createPackage(CreatePackageRequest request, String userId) {
        log.info("Creating package for user: {}", userId);

        validateCreatePackage(request, userId);

        Type type = typeRepository.findByName(request.getType())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Type not found: " + request.getType()));

        Package packageEntity = packageMapper.toEntity(request);
        packageEntity.setUserId(userId);
        packageEntity.setType(type);
        packageEntity.setPublishStatus(PublishStatus.DRAFT); // Luôn DRAFT khi tạo mới
        if (request.getCategory() != null) {
            packageEntity.setCategory(request.getCategory());
        }

        beforeSavePackage(packageEntity, request);

        Package saved = packageRepository.save(packageEntity);

        afterSavePackage(saved);

        return packageMapper.toResponse(saved);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PackageResponse getPackageById(String id, String userId, Set<String> roles, String ticketModule) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        if (isPrivileged(roles)) {
            return packageMapper.toResponse(packageEntity);
        }

        if (packageEntity.getPublishStatus() == PublishStatus.PUBLISHED) {
            return packageMapper.toResponse(packageEntity);
        }

        boolean canReadOwnDraft = userId != null
                && userId.equals(packageEntity.getUserId())
                && hasModuleTicket(userId, ticketModule);
        if (canReadOwnDraft) {
            return packageMapper.toResponse(packageEntity);
        }

        // Ẩn sự tồn tại của package DRAFT với user không đủ quyền
        throw new ApiException(ErrorCode.E227, "Package not found");
    }

    @Override
    @Transactional(readOnly = true)
    public PackageResponse getPackageById(String id) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));
        return packageMapper.toResponse(packageEntity);
    }

    /**
     * GET phân quyền:
     * - ADMIN/MANAGER → tất cả
     * - ticket-holder đúng module → PUBLISHED + DRAFT do mình tạo
     * - Người thường → chỉ PUBLISHED
     */
    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getAllPackages(String userId, Set<String> roles, String ticketModule) {
        if (isPrivileged(roles)) {
            return packageMapper.toResponseList(packageRepository.findAll());
        }
        if (hasModuleTicket(userId, ticketModule)) {
            return packageMapper.toResponseList(
                    packageRepository.findAllForTicketHolder(userId));
        }
        return packageMapper.toResponseList(
                packageRepository.findAllByPublishStatus(PublishStatus.PUBLISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getPackagesByType(TypeName type, String userId, Set<String> roles,
            String ticketModule) {
        if (isPrivileged(roles)) {
            return packageMapper.toResponseList(packageRepository.findByTypeName(type));
        }
        if (hasModuleTicket(userId, ticketModule)) {
            return packageMapper.toResponseList(
                    packageRepository.findByTypeNameForTicketHolder(type, userId));
        }
        return packageMapper.toResponseList(
                packageRepository.findByTypeNameAndPublishStatus(type, PublishStatus.PUBLISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getPackagesByTypeAndCategory(TypeName typeName, CategoryType category,
            String userId, Set<String> roles, String ticketModule) {
        if (isPrivileged(roles)) {
            return packageMapper.toResponseList(
                    packageRepository.findByTypeNameAndCategory(typeName, category));
        }
        if (hasModuleTicket(userId, ticketModule)) {
            return packageMapper.toResponseList(
                    packageRepository.findByTypeNameAndCategoryForTicketHolder(typeName, category, userId));
        }
        return packageMapper.toResponseList(
                packageRepository.findByTypeNameAndCategoryAndPublishStatus(typeName, category,
                        PublishStatus.PUBLISHED));
    }

    // ── Legacy methods (không filter — giữ tương thích nội bộ) ───────────────

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getAllPackages() {
        return packageMapper.toResponseList(packageRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getPackagesByType(TypeName typeName) {
        return packageMapper.toResponseList(packageRepository.findByTypeName(typeName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getPackagesByTypeAndCategory(TypeName typeName, CategoryType category) {
        return packageMapper.toResponseList(
                packageRepository.findByTypeNameAndCategory(typeName, category));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Override
    public PackageResponse updatePackage(String id, UpdatePackageRequest request, String userId) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        validateUpdatePackage(packageEntity, request, userId);

        if (request.getName() != null) {
            packageEntity.setName(request.getName());
        }
        if (request.getType() != null) {
            Type type = typeRepository.findByName(request.getType())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Type not found: " + request.getType()));
            packageEntity.setType(type);
        }
        if (request.getDescription() != null) {
            packageEntity.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            packageEntity.setCategory(request.getCategory());
        }
        if (request.getThumbnail() != null) {
            packageEntity.setThumbnail(request.getThumbnail());
        }
        if (request.getPrice() != null) {
            packageEntity.setPrice(request.getPrice());
        }
        if (request.getPricingType() != null) {
            packageEntity.setPricingType(request.getPricingType());
        }

        beforeUpdatePackage(packageEntity, request);

        Package updated = packageRepository.save(packageEntity);

        // Package đã publish mà bị chỉnh sửa metadata/content ở cấp package
        // => bắt buộc quay về DRAFT để duyệt lại.
        revertToDraft(updated.getId(), userId, "CONTENT_UPDATED");
        Package latest = packageRepository.findById(updated.getId()).orElse(updated);

        afterUpdatePackage(latest);

        return packageMapper.toResponse(latest);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Override
    public void deletePackage(String id, String userId) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        beforeDeletePackage(packageEntity, userId);

        List<Folder> folders = folderRepository.findByPackageId(id);
        for (Folder folder : folders) {
            List<com.lms.content.common.entity.StudySet> studySets = folder.getStudySets();
            if (studySets != null && !studySets.isEmpty()) {
                List<com.lms.content.common.entity.StudySet> setsToDelete = List.copyOf(studySets);
                for (com.lms.content.common.entity.StudySet set : setsToDelete) {
                    try {
                        studySetDeletionService.deleteStudySetInNewTransaction(set.getId(), userId);
                    } catch (Exception e) {
                        log.warn("Failed to delete study set {} during package deletion", set.getId(), e);
                    }
                }
            }
            folderService.deleteFolder(folder.getId(), userId);
        }

        folderRepository.flush();
        packageRepository.delete(packageEntity);

        afterDeletePackage(id, userId);
    }

    // ── FOLDER MANAGEMENT ─────────────────────────────────────────────────────

    @Override
    public PackageResponse addFolderToPackage(String packageId, String folderId, String userId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        packageEntity.addFolder(folder);
        Package updated = packageRepository.save(packageEntity);

        // Thao tác ở tầng dưới package (folder) => quay về DRAFT nếu đang PUBLISHED
        revertToDraft(updated.getId(), userId, "CONTENT_UPDATED");

        return packageMapper.toResponse(updated);
    }

    @Override
    public PackageResponse removeFolderFromPackage(String packageId, String folderId, String userId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        packageEntity.removeFolder(folder);
        Package updated = packageRepository.save(packageEntity);

        // Thao tác ở tầng dưới package (folder) => quay về DRAFT nếu đang PUBLISHED
        revertToDraft(updated.getId(), userId, "CONTENT_UPDATED");

        return packageMapper.toResponse(updated);
    }

    // ── PAGED QUERIES ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getLatestPackages(TypeName type, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        List<Package> packages = packageRepository.findByTypeNameAndPublishStatus(type, PublishStatus.PUBLISHED,
                pageRequest);
        return packageMapper.toResponseList(packages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getMostEnrolledPackages(TypeName type, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("enrollmentCount").descending());
        List<Package> packages = packageRepository.findByTypeNameAndPublishStatus(type, PublishStatus.PUBLISHED,
                pageRequest);
        return packageMapper.toResponseList(packages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getFreePackages(TypeName type, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        List<Package> packages = packageRepository.findByTypeNameAndPricingTypeAndPublishStatus(
                type, "FREE", PublishStatus.PUBLISHED, pageRequest);
        return packageMapper.toResponseList(packages);
    }

    @Override
    public long countPublishedPackagesByType(TypeName type) {
        return packageRepository.countByTypeNameAndPublishStatus(type, PublishStatus.PUBLISHED);
    }

    // ── PUBLISH WORKFLOW ──────────────────────────────────────────────────────

    @Override
    public PackageResponse publishPackage(String packageId, String userId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        packageEntity.setPublishStatus(PublishStatus.PUBLISHED);
        Package saved = packageRepository.save(packageEntity);

        log.info("Package {} published by userId={}", packageId, userId);
        return packageMapper.toResponse(saved);
    }

    @Override
    public PackageResponse unpublishPackage(String packageId, String userId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        packageEntity.setPublishStatus(PublishStatus.DRAFT);
        Package saved = packageRepository.save(packageEntity);

        log.info("Package {} unpublished (DRAFT) by userId={}", packageId, userId);
        return packageMapper.toResponse(saved);
    }

    /**
     * Revert package về DRAFT khi nội dung bên trong bị CUD.
     * Idempotent — package đã DRAFT thì không làm gì.
     */
    @Override
    public void revertToDraft(String packageId, String triggeredBy, String reason) {
        packageRepository.findById(packageId).ifPresent(pkg -> {
            if (pkg.getPublishStatus() == PublishStatus.PUBLISHED) {
                pkg.setPublishStatus(PublishStatus.DRAFT);
                packageRepository.save(pkg);
                log.info("Package {} auto-reverted to DRAFT. Reason={}, triggeredBy={}",
                        packageId, reason, triggeredBy);
            }
        });
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private boolean isPrivileged(Set<String> roles) {
        if (roles == null || roles.isEmpty())
            return false;
        return roles.stream().anyMatch(r -> PRIVILEGED_ROLES.contains(r.toUpperCase()));
    }

    private boolean hasModuleTicket(String userId, String ticketModule) {
        if (userId == null || userId.isBlank() || ticketModule == null || ticketModule.isBlank()) {
            return false;
        }
        return ticketAccessClient.checkAccess(userId, ticketModule);
    }

    // ── EXTENSION HOOKS ───────────────────────────────────────────────────────

    protected void validateCreatePackage(CreatePackageRequest request, String userId) {
    }

    protected void beforeSavePackage(Package packageEntity, CreatePackageRequest request) {
    }

    protected void afterSavePackage(Package saved) {
    }

    protected void validateUpdatePackage(Package entity, UpdatePackageRequest request, String userId) {
    }

    protected void beforeUpdatePackage(Package entity, UpdatePackageRequest request) {
    }

    protected void afterUpdatePackage(Package updated) {
    }

    protected void beforeDeletePackage(Package entity, String userId) {
    }

    protected void afterDeletePackage(String packageId, String userId) {
    }
}
