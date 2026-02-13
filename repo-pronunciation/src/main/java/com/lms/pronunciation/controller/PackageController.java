package com.lms.pronunciation.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.dto.response.TypeResponse;
import com.lms.content.common.entity.TypeName;
import com.lms.pronunciation.service.ExcelImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageApiDelegate delegate;
    private final ExcelImportService excelImportService;

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<TypeResponse>>> getPackageTypes() {
        List<TypeResponse> types = delegate.getPackageTypes();
        return ResponseEntity.ok(ApiResponse.ok(types));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @RequestBody @Valid CreatePackageRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = delegate.createPackage(request, principal.userId());
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
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = delegate.updatePackage(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        delegate.deletePackage(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{packageId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<PackageResponse>> addFolderToPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = delegate.addFolderToPackage(packageId, folderId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{packageId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<PackageResponse>> removeFolderFromPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = delegate.removeFolderFromPackage(packageId, folderId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/import-excel")
    public ResponseEntity<ApiResponse<HierarchicalImportResult>> importFromPackageExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("packageTypeId") String packageTypeId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        HierarchicalImportResult response = excelImportService.importFromPackageExcel(file, packageTypeId,
                principal.userId(), false);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
