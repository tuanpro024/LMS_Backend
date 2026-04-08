package com.lms.multimedia.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.dto.response.TypeResponse;
import com.lms.content.common.entity.TypeName;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Thin controller that delegates to PackageApiDelegate from content-common
 * Handles HTTP concerns only (status codes, response wrapping)
 */
@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageApiDelegate delegate;

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<TypeResponse>>> getPackageTypes() {
        List<TypeResponse> types = delegate.getPackageTypes();
        return ResponseEntity.ok(ApiResponse.ok(types));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @RequestBody @Valid CreatePackageRequest request,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        PackageResponse response = delegate.createPackage(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackageById(@PathVariable String id) {
        PackageResponse response = delegate.getPackageById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getPackages(
            @RequestParam(required = false) TypeName type) {
        List<PackageResponse> response = (type != null)
                ? delegate.getPackagesByType(type)
                : delegate.getAllPackages();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(
            @PathVariable String id,
            @RequestBody @Valid UpdatePackageRequest request,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        PackageResponse response = delegate.updatePackage(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @PathVariable String id,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        delegate.deletePackage(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{packageId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<PackageResponse>> addFolderToPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        PackageResponse response = delegate.addFolderToPackage(packageId, folderId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{packageId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<PackageResponse>> removeFolderFromPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        PackageResponse response = delegate.removeFolderFromPackage(packageId, folderId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
