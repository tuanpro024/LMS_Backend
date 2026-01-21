package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.CreatePackageRequest;
import com.lms.flashcard.dto.request.UpdatePackageRequest;
import com.lms.flashcard.dto.response.PackageResponse;
import com.lms.flashcard.entity.Folder;
import com.lms.flashcard.entity.Package;
import com.lms.flashcard.entity.Type;
import com.lms.flashcard.entity.TypeName;
import com.lms.flashcard.mapper.PackageMapper;
import com.lms.flashcard.repository.FolderRepository;
import com.lms.flashcard.repository.PackageRepository;
import com.lms.flashcard.repository.TypeRepository;
import com.lms.flashcard.service.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final FolderRepository folderRepository;
    private final PackageMapper packageMapper;
    private final TypeRepository typeRepository;

    @Override
    public PackageResponse createPackage(CreatePackageRequest request, String userId) {
        log.info("Creating package for user: {}", userId);

        // Fetch Type entity from TypeRepository
        Type type = typeRepository.findByName(request.getType())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Type not found: " + request.getType()));

        Package packageEntity = packageMapper.toEntity(request);
        packageEntity.setUserId(userId);
        packageEntity.setType(type);

        Package saved = packageRepository.save(packageEntity);
        return packageMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageResponse getPackageById(String id) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        return packageMapper.toResponse(packageEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getAllPackages() {
        List<Package> packages = packageRepository.findAll();
        return packageMapper.toResponseList(packages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getPackagesByType(TypeName typeName) {
        // Fetch Type entity from TypeRepository
        Type type = typeRepository.findByName(typeName)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Type not found: " + typeName));

        List<Package> packages = packageRepository.findByType(type);
        return packageMapper.toResponseList(packages);
    }

    @Override
    public PackageResponse updatePackage(String id, UpdatePackageRequest request, String userId) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        // Check ownership
        if (!packageEntity.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this package");
        }

        // Update fields if provided
        if (request.getName() != null) {
            packageEntity.setName(request.getName());
        }
        if (request.getType() != null) {
            Type type = typeRepository.findByName(request.getType())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Type not found: " + request.getType()));
            packageEntity.setType(type);
        }
        if (request.getDescription() != null) {
            packageEntity.setDescription(request.getDescription());
        }

        Package updated = packageRepository.save(packageEntity);
        return packageMapper.toResponse(updated);
    }

    @Override
    public void deletePackage(String id, String userId) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        // Check ownership
        if (!packageEntity.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this package");
        }

        // Cascade delete will handle subjects and their slots automatically
        packageRepository.delete(packageEntity);
    }

    @Override
    public PackageResponse addFolderToPackage(String packageId, String folderId, String userId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        // Check ownership of package
        if (!packageEntity.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this package");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership of folder
        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add this folder");
        }

        packageEntity.addFolder(folder);
        Package updated = packageRepository.save(packageEntity);

        return packageMapper.toResponse(updated);
    }

    @Override
    public PackageResponse removeFolderFromPackage(String packageId, String folderId, String userId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        // Check ownership
        if (!packageEntity.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this package");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        packageEntity.removeFolder(folder);
        Package updated = packageRepository.save(packageEntity);

        return packageMapper.toResponse(updated);
    }
}
