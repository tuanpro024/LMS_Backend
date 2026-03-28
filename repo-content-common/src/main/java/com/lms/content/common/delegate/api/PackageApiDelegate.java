package com.lms.content.common.delegate.api;

import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.dto.response.TypeResponse;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.repository.TypeRepository;
import com.lms.content.common.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API Delegate for Package operations
 * Returns pure DTOs - NO ResponseEntity
 * Controllers handle HTTP concerns (status codes, headers)
 */
@Component
@RequiredArgsConstructor
public class PackageApiDelegate {

    private final PackageService packageService;
    private final TypeRepository typeRepository;

    /**
     * Get all available package types
     * 
     * @return List of TypeResponse with IDs and names
     */
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
     * Get packages by type and category
     * 
     * @return List of PackageResponse DTOs
     */
    public List<PackageResponse> getPackagesByTypeAndCategory(TypeName type,
            com.lms.content.common.entity.CategoryType category) {
        return packageService.getPackagesByTypeAndCategory(type, category);
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

    /**
     * Get latest packages
     * 
     * @return List of PackageResponse DTOs
     */
    public List<PackageResponse> getLatestPackages(TypeName type, int limit) {
        return packageService.getLatestPackages(type, limit);
    }

    /**
     * Get most enrolled packages
     * 
     * @return List of PackageResponse DTOs
     */
    public List<PackageResponse> getMostEnrolledPackages(TypeName type, int limit) {
        return packageService.getMostEnrolledPackages(type, limit);
    }

    /**
     * Get free packages
     * 
     * @return List of PackageResponse DTOs
     */
    public List<PackageResponse> getFreePackages(TypeName type, int limit) {
        return packageService.getFreePackages(type, limit);
    }
}
