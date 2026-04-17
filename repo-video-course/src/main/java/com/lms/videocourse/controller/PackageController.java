package com.lms.videocourse.controller;

import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.entity.CategoryType;
import com.lms.videocourse.dto.response.SyllabusCategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for managing Packages in the Video Course context.
 * A Package groups Folders (e.g. "Video HSK", "Video JLPT").
 * Delegates to PackageApiDelegate from repo-content-common.
 */
@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private static final String TICKET_MODULE = TicketModuleEnum.VIDEO_COURSE.name();

    private final PackageApiDelegate packageDelegate;

    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @RequestBody @Valid CreatePackageRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = packageDelegate.createPackage(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getLatestPackages(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.getLatestPackages(TypeName.VIDEO_COURSE, limit)));
    }

    @GetMapping("/most-enrolled")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getMostEnrolledPackages(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.getMostEnrolledPackages(TypeName.VIDEO_COURSE, limit)));
    }

    @GetMapping("/free")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getFreePackages(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.getFreePackages(TypeName.VIDEO_COURSE, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackage(
            @PathVariable String id,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        Set<String> roles = extractRoles(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                packageDelegate.getPackageById(id, userId, roles, TICKET_MODULE)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAllPackages(
            @RequestParam(required = false) TypeName type,
            @RequestParam(required = false) com.lms.content.common.entity.CategoryType category,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        Set<String> roles = extractRoles(authentication);
        List<PackageResponse> response;
        if (type != null && category != null) {
            response = packageDelegate.getPackagesByTypeAndCategory(type, category, userId, roles, TICKET_MODULE);
        } else if (type != null) {
            response = packageDelegate.getPackagesByType(type, userId, roles, TICKET_MODULE);
        } else {
            response = packageDelegate.getAllPackages(userId, roles, TICKET_MODULE);
        }
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<SyllabusCategoryResponse>>> getCategories() {
        List<SyllabusCategoryResponse> categories = Arrays.stream(CategoryType.values())
                .map(cat -> SyllabusCategoryResponse.builder()
                        .code(cat.name())
                        .name(cat.getDisplayName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(
            @PathVariable String id,
            @RequestBody @Valid UpdatePackageRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.updatePackage(id, request, principal.userId())));
    }

    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        packageDelegate.deletePackage(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{packageId}/folders/{folderId}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<PackageResponse>> addFolderToPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                packageDelegate.addFolderToPackage(packageId, folderId, principal.userId())));
    }

    @DeleteMapping("/{packageId}/folders/{folderId}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<PackageResponse>> removeFolderFromPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                packageDelegate.removeFolderFromPackage(packageId, folderId, principal.userId())));
    }

    @PostMapping("/{id}/publish")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<PackageResponse>> publishPackage(
            @PathVariable String id, Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.publishPackage(id, principal.userId())));
    }

    @PostMapping("/{id}/unpublish")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<PackageResponse>> unpublishPackage(
            @PathVariable String id, Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.unpublishPackage(id, principal.userId())));
    }

    private String extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return null;
        if (authentication.getPrincipal() instanceof AuthPrincipal p)
            return p.userId();
        return null;
    }

    private Set<String> extractRoles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return Collections.emptySet();
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());
    }
}
