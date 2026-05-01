package com.lms.content.common.service;

import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.CategoryType;
import com.lms.content.common.entity.TypeName;

import java.util.List;
import java.util.Set;

public interface PackageService {

    PackageResponse createPackage(CreatePackageRequest request, String userId);

    /**
     * Lấy package theo id với phân quyền theo role + ticket module.
     * - ADMIN/MANAGER: xem mọi status
     * - Ticket-holder đúng module: xem PUBLISHED + DRAFT do chính mình tạo
     * - User thường: chỉ xem PUBLISHED
     */
    PackageResponse getPackageById(String id, String userId, Set<String> roles, String ticketModule);

    // Legacy (không filter theo publish status) cho các luồng nội bộ.
    PackageResponse getPackageById(String id);

    // ── Context-aware GET: phân quyền theo role/ticket ────────────────────────

    /**
     * Lấy tất cả package theo quyền của user:
     * <ul>
     * <li>ADMIN/MANAGER → tất cả</li>
     * <li>ticket-holder → PUBLISHED + DRAFT do mình tạo</li>
     * <li>Người thường → chỉ PUBLISHED</li>
     * </ul>
     */
    List<PackageResponse> getAllPackages(String userId, Set<String> roles, String ticketModule);

    List<PackageResponse> getPackagesByType(TypeName type, String userId, Set<String> roles, String ticketModule);

    List<PackageResponse> getPackagesByTypeAndCategory(TypeName type, CategoryType category,
            String userId, Set<String> roles, String ticketModule);

    // ── Legacy (giữ tương thích nội bộ) ───────────────────────────────────────

    List<PackageResponse> getAllPackages();

    List<PackageResponse> getPackagesByType(TypeName type);

    List<PackageResponse> getPackagesByTypeAndCategory(TypeName type, CategoryType category);

    // ── CUD ───────────────────────────────────────────────────────────────────

    PackageResponse updatePackage(String id, UpdatePackageRequest request, String userId);

    void deletePackage(String id, String userId);

    PackageResponse addFolderToPackage(String packageId, String folderId, String userId);

    PackageResponse removeFolderFromPackage(String packageId, String folderId, String userId);

    List<PackageResponse> getLatestPackages(TypeName type, int limit);

    List<PackageResponse> getMostEnrolledPackages(TypeName type, int limit);

    List<PackageResponse> getFreePackages(TypeName type, int limit);

    long countPublishedPackagesByType(TypeName type);

    // ── Publish workflow ───────────────────────────────────────────────────────

    /**
     * Publish package (chỉ ADMIN/MANAGER được gọi).
     */
    PackageResponse publishPackage(String packageId, String userId);

    /**
     * Unpublish package về DRAFT (chỉ ADMIN/MANAGER được gọi).
     */
    PackageResponse unpublishPackage(String packageId, String userId);

    /**
     * Tự động revert package về DRAFT khi nội dung bên trong bị CUD.
     * Idempotent: nếu đã DRAFT thì không làm gì.
     *
     * @param packageId   ID của package cần revert
     * @param triggeredBy userId thực hiện thao tác
     * @param reason      lý do ("CONTENT_UPDATED")
     */
    void revertToDraft(String packageId, String triggeredBy, String reason);
}
