package com.lms.content.common.delegate.api;

import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.dto.response.TypeResponse;
import com.lms.content.common.entity.CategoryType;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.repository.TypeRepository;
import com.lms.content.common.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API Delegate cho Package operations.
 * Trả về pure DTO — Controller xử lý HTTP concerns (status codes, headers).
 */
@Component
@RequiredArgsConstructor
public class PackageApiDelegate {

    private final PackageService packageService;
    private final TypeRepository typeRepository;

    // ── TYPES ─────────────────────────────────────────────────────────────────

    public List<TypeResponse> getPackageTypes() {
        return typeRepository.findAll().stream()
                .map(type -> TypeResponse.builder()
                        .id(type.getId())
                        .name(type.getName())
                        .description(type.getDescription())
                        .createdAt(type.getCreatedAt())
                        .updatedAt(type.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public PackageResponse createPackage(CreatePackageRequest request, String userId) {
        return packageService.createPackage(request, userId);
    }

    // ── CONTEXT-AWARE GET (phân quyền theo role / ticket) ─────────────────────

    /**
     * Lấy package theo id với phân quyền theo role + module ticket.
     */
    public PackageResponse getPackageById(String id, String userId, Set<String> roles, String ticketModule) {
        return packageService.getPackageById(id, userId, roles, ticketModule);
    }

    /**
     * Legacy nội bộ: không filter publishStatus.
     */
    public PackageResponse getPackageById(String id) {
        return packageService.getPackageById(id);
    }

    /**
     * Lấy tất cả package với phân quyền:
     * <ul>
     * <li>ADMIN/MANAGER → tất cả</li>
     * <li>ticket-holder → PUBLISHED + DRAFT do mình tạo</li>
     * <li>Anonymous/user thường → chỉ PUBLISHED</li>
     * </ul>
     *
     * @param userId null nếu anonymous
     * @param roles  empty set nếu anonymous
     */
    public List<PackageResponse> getAllPackages(String userId, Set<String> roles, String ticketModule) {
        return packageService.getAllPackages(userId, roles, ticketModule);
    }

    /** @deprecated Dùng {@link #getAllPackages(String, Set, String)} */
    @Deprecated
    public List<PackageResponse> getAllPackages() {
        return packageService.getAllPackages();
    }

    public List<PackageResponse> getPackagesByType(TypeName type, String userId, Set<String> roles,
            String ticketModule) {
        return packageService.getPackagesByType(type, userId, roles, ticketModule);
    }

    /**
     * @deprecated Dùng {@link #getPackagesByType(TypeName, String, Set, String)}
     */
    @Deprecated
    public List<PackageResponse> getPackagesByType(TypeName type) {
        return packageService.getPackagesByType(type);
    }

    public List<PackageResponse> getPackagesByTypeAndCategory(TypeName type, CategoryType category,
            String userId, Set<String> roles, String ticketModule) {
        return packageService.getPackagesByTypeAndCategory(type, category, userId, roles, ticketModule);
    }

    /**
     * @deprecated Dùng
     *             {@link #getPackagesByTypeAndCategory(TypeName, CategoryType, String, Set, String)}
     */
    @Deprecated
    public List<PackageResponse> getPackagesByTypeAndCategory(TypeName type, CategoryType category) {
        return packageService.getPackagesByTypeAndCategory(type, category);
    }

    // ── UPDATE / DELETE ───────────────────────────────────────────────────────

    public PackageResponse updatePackage(String id, UpdatePackageRequest request, String userId) {
        return packageService.updatePackage(id, request, userId);
    }

    public void deletePackage(String id, String userId) {
        packageService.deletePackage(id, userId);
    }

    public PackageResponse addFolderToPackage(String packageId, String folderId, String userId) {
        return packageService.addFolderToPackage(packageId, folderId, userId);
    }

    public PackageResponse removeFolderFromPackage(String packageId, String folderId, String userId) {
        return packageService.removeFolderFromPackage(packageId, folderId, userId);
    }

    // ── PAGED ─────────────────────────────────────────────────────────────────

    public List<PackageResponse> getLatestPackages(TypeName type, int limit) {
        return packageService.getLatestPackages(type, limit);
    }

    public List<PackageResponse> getMostEnrolledPackages(TypeName type, int limit) {
        return packageService.getMostEnrolledPackages(type, limit);
    }

    public List<PackageResponse> getFreePackages(TypeName type, int limit) {
        return packageService.getFreePackages(type, limit);
    }

    public long countPublishedPackagesByType(TypeName type) {
        return packageService.countPublishedPackagesByType(type);
    }

    // ── PUBLISH WORKFLOW ──────────────────────────────────────────────────────

    /**
     * Publish package → PUBLISHED (chỉ ADMIN/MANAGER gọi, @PreAuthorize ở
     * controller).
     */
    public PackageResponse publishPackage(String id, String userId) {
        return packageService.publishPackage(id, userId);
    }

    /**
     * Unpublish package → DRAFT (chỉ ADMIN/MANAGER gọi, @PreAuthorize ở
     * controller).
     */
    public PackageResponse unpublishPackage(String id, String userId) {
        return packageService.unpublishPackage(id, userId);
    }
}
