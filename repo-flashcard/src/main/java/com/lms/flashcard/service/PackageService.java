package com.lms.flashcard.service;

import com.lms.flashcard.dto.request.CreatePackageRequest;
import com.lms.flashcard.dto.request.UpdatePackageRequest;
import com.lms.flashcard.dto.response.PackageResponse;
import com.lms.flashcard.entity.TypeName;

import java.util.List;

public interface PackageService {

    PackageResponse createPackage(CreatePackageRequest request, String userId);

    PackageResponse getPackageById(String id);

    List<PackageResponse> getAllPackages();

    List<PackageResponse> getPackagesByType(TypeName type);

    PackageResponse updatePackage(String id, UpdatePackageRequest request, String userId);

    void deletePackage(String id, String userId);

    PackageResponse addFolderToPackage(String packageId, String folderId, String userId);

    PackageResponse removeFolderFromPackage(String packageId, String folderId, String userId);
}
