package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.entity.CategoryType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing Packages in the Video Course context.
 * A Package groups Folders (e.g. "Video HSK", "Video JLPT").
 * Delegates to PackageApiDelegate from repo-content-common.
 */
@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageApiDelegate packageDelegate;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_TEACHER_MANAGER')")
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
    public ResponseEntity<ApiResponse<PackageResponse>> getPackage(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.getPackageById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAllPackages(
            @RequestParam(required = false) TypeName type,
            @RequestParam(required = false) com.lms.content.common.entity.CategoryType category) {

        List<PackageResponse> response;
        if (type != null && category != null) {
            response = packageDelegate.getPackagesByTypeAndCategory(type, category);
        } else if (type != null) {
            response = packageDelegate.getPackagesByType(type);
        } else {
            response = packageDelegate.getAllPackages();
        }
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<com.lms.videocourse.dto.response.CategoryResponse>>> getCategories() {
        List<com.lms.videocourse.dto.response.CategoryResponse> categories = java.util.Arrays
                .stream(com.lms.content.common.entity.CategoryType.values())
                .map(cat -> com.lms.videocourse.dto.response.CategoryResponse.builder()
                        .code(cat.name())
                        .name(cat.getDisplayName())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(
            @PathVariable String id,
            @RequestBody @Valid UpdatePackageRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.updatePackage(id, request, principal.userId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        packageDelegate.deletePackage(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{packageId}/folders/{folderId}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<PackageResponse>> addFolderToPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                packageDelegate.addFolderToPackage(packageId, folderId, principal.userId())));
    }

    @DeleteMapping("/{packageId}/folders/{folderId}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<PackageResponse>> removeFolderFromPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                packageDelegate.removeFolderFromPackage(packageId, folderId, principal.userId())));
    }
}
