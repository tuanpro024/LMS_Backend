package com.lms.content.common.service;

import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.TypeName;

import java.util.List;

public interface PackageService {

    PackageResponse createPackage(CreatePackageRequest request, String userId);

    PackageResponse getPackageById(String id);

    List<PackageResponse> getAllPackages();

    List<PackageResponse> getPackagesByType(TypeName type);

    List<PackageResponse> getPackagesByTypeAndCategory(TypeName type,
            com.lms.content.common.entity.CategoryType category);

    PackageResponse updatePackage(String id, UpdatePackageRequest request, String userId);

    void deletePackage(String id, String userId);

    PackageResponse addFolderToPackage(String packageId, String folderId, String userId);

    PackageResponse removeFolderFromPackage(String packageId, String folderId, String userId);

    List<PackageResponse> getLatestPackages(TypeName type, int limit);

    List<PackageResponse> getMostEnrolledPackages(TypeName type, int limit);

    List<PackageResponse> getFreePackages(TypeName type, int limit);
}
