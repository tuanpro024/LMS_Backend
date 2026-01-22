package com.lms.content.common.delegate.api;

import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * API Delegate for Package operations
 * Returns pure DTOs - NO ResponseEntity
 * Controllers handle HTTP concerns (status codes, headers)
 */
@Component
@RequiredArgsConstructor
public class PackageApiDelegate {

    private final PackageService packageService;

    /**
     * Get all available package types
     * 
     * @return List of TypeName enums
     */
    public List<TypeName> getPackageTypes() {
        return Arrays.asList(TypeName.values());
    }

    /**
     * Create a new package
     * 
     * @return PackageResponse DTO (controller decides HTTP 201 vs 200)
     */
    public PackageResponse createPackage(CreatePackageRequest request, String userId) {
        return packageService.createPackage(request, userId);
    }

    /**
     * Get package by ID
     * 
     * @return PackageResponse DTO
     */
    public PackageResponse getPackageById(String id) {
        return packageService.getPackageById(id);
    }

    /**
     * Get all packages
     * 
     * @return List of PackageResponse DTOs
     */
    public List<PackageResponse> getAllPackages() {
        return packageService.getAllPackages();
    }

    /**
     * Get packages by type
     * 
     * @return List of PackageResponse DTOs
     */
    public List<PackageResponse> getPackagesByType(TypeName type) {
        return packageService.getPackagesByType(type);
    }

    /**
     * Update package
     * 
     * @return Updated PackageResponse DTO
     */
    public PackageResponse updatePackage(String id, UpdatePackageRequest request, String userId) {
        return packageService.updatePackage(id, request, userId);
    }

    /**
     * Delete package
     * Returns void - controller decides response structure
     */
    public void deletePackage(String id, String userId) {
        packageService.deletePackage(id, userId);
    }

    /**
     * Add folder to package
     * 
     * @return Updated PackageResponse DTO
     */
    public PackageResponse addFolderToPackage(String packageId, String folderId, String userId) {
        return packageService.addFolderToPackage(packageId, folderId, userId);
    }

    /**
     * Remove folder from package
     * 
     * @return Updated PackageResponse DTO
     */
    public PackageResponse removeFolderFromPackage(String packageId, String folderId, String userId) {
        return packageService.removeFolderFromPackage(packageId, folderId, userId);
    }
}
