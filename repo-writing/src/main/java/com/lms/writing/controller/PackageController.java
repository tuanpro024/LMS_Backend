package com.lms.writing.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.writing.dto.request.CreatePackageRequest;
import com.lms.writing.dto.request.UpdatePackageRequest;
import com.lms.writing.dto.response.PackageResponse;
import com.lms.writing.entity.TypeName;
import com.lms.writing.service.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
@Slf4j
public class PackageController {

    private final PackageService packageService;

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<TypeName>>> getPackageTypes() {
        List<TypeName> types = Arrays.asList(TypeName.values());
        return ResponseEntity.ok(ApiResponse.ok(types));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @RequestBody @Valid CreatePackageRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = packageService.createPackage(request, principal.userId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackage(
            @PathVariable String id) {

        PackageResponse response = packageService.getPackageById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAllPackages() {

        List<PackageResponse> response = packageService.getAllPackages();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getPackagesByType(
            @PathVariable TypeName type) {

        List<PackageResponse> response = packageService.getPackagesByType(type);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(
            @PathVariable String id,
            @RequestBody @Valid UpdatePackageRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = packageService.updatePackage(id, request, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        packageService.deletePackage(id, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{packageId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<PackageResponse>> addFolderToPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = packageService.addFolderToPackage(packageId, folderId, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{packageId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<PackageResponse>> removeFolderFromPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = packageService.removeFolderFromPackage(packageId, folderId, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
