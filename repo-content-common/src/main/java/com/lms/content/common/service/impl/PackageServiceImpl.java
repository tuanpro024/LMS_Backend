package com.lms.content.common.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.Type;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.mapper.PackageMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.TypeRepository;
import com.lms.content.common.service.PackageService;
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
    private final com.lms.content.common.service.FolderService folderService;

    @Override
    public PackageResponse createPackage(CreatePackageRequest request, String userId) {
        log.info("Creating package for user: {}", userId);

        // Hook: validate before processing
        validateCreatePackage(request, userId);

        // Fetch Type entity from TypeRepository
        Type type = typeRepository.findByName(request.getType())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Type not found: " + request.getType()));

        Package packageEntity = packageMapper.toEntity(request);
        packageEntity.setUserId(userId);
        packageEntity.setType(type);
        if (request.getCategory() != null) {
            packageEntity.setCategory(request.getCategory());
        }

        // Hook: customize before save
        beforeSavePackage(packageEntity, request);

        Package saved = packageRepository.save(packageEntity);

        // Hook: post-processing
        afterSavePackage(saved);

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
        List<Package> packages = packageRepository.findByTypeName(typeName);
        return packageMapper.toResponseList(packages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getPackagesByTypeAndCategory(TypeName typeName,
            com.lms.content.common.entity.CategoryType category) {
        List<Package> packages = packageRepository.findByTypeNameAndCategory(typeName, category);
        return packageMapper.toResponseList(packages);
    }

    @Override
    public PackageResponse updatePackage(String id, UpdatePackageRequest request, String userId) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        // Check ownership
        // Check ownership - DISABLED to allow Admin/Manager access
        // if (!packageEntity.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to modify this
        // package");
        // }

        // Hook: validate before update
        validateUpdatePackage(packageEntity, request, userId);

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
        if (request.getCategory() != null) {
            packageEntity.setCategory(request.getCategory());
        }
        if (request.getThumbnail() != null) {
            packageEntity.setThumbnail(request.getThumbnail());
        }
        if (request.getPrice() != null) {
            packageEntity.setPrice(request.getPrice());
        }
        if (request.getPricingType() != null) {
            packageEntity.setPricingType(request.getPricingType());
        }

        // Hook: before update save
        beforeUpdatePackage(packageEntity, request);

        Package updated = packageRepository.save(packageEntity);

        // Hook: after update
        afterUpdatePackage(updated);

        return packageMapper.toResponse(updated);
    }

    @Override
    public void deletePackage(String id, String userId) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        // Check ownership
        // Check ownership - DISABLED to allow Admin/Manager access
        // if (!packageEntity.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to delete this
        // package");
        // }

        // Hook: before delete
        beforeDeletePackage(packageEntity, userId);

        // Cascade delete will handle subjects and their slots automatically
        // But we need to manually trigger folder cleanup to ensure StudySets/Videos are
        // handled via listeners
        List<Folder> folders = folderRepository.findByPackageId(id);
        for (Folder folder : folders) {
            try {
                folderService.deleteFolder(folder.getId(), userId);
            } catch (Exception e) {
                log.error("Failed to delete folder {} during package deletion: {}", folder.getId(), e.getMessage());
            }
        }

        packageRepository.delete(packageEntity);

        // Hook: after delete
        afterDeletePackage(id, userId);
    }

    @Override
    public PackageResponse addFolderToPackage(String packageId, String folderId, String userId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        // Check ownership of package
        // Check ownership of package - DISABLED
        // if (!packageEntity.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to modify this
        // package");
        // }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership of folder
        // Check ownership of folder - DISABLED
        // if (!folder.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to add this folder");
        // }

        packageEntity.addFolder(folder);
        Package updated = packageRepository.save(packageEntity);

        return packageMapper.toResponse(updated);
    }

    @Override
    public PackageResponse removeFolderFromPackage(String packageId, String folderId, String userId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Package not found"));

        // Check ownership
        // Check ownership - DISABLED
        // if (!packageEntity.getUserId().equals(userId)) {
        // throw new ApiException(ErrorCode.E240, "No permission to modify this
        // package");
        // }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        packageEntity.removeFolder(folder);
        Package updated = packageRepository.save(packageEntity);

        return packageMapper.toResponse(updated);
    }

    // ========== EXTENSION HOOKS (protected, non-final) ==========

    /**
     * Hook: Validate before creating package
     * Can be overridden in specific services for custom validation
     */
    protected void validateCreatePackage(CreatePackageRequest request, String userId) {
        // Default implementation - empty
        // Override in flashcard/writing services if needed
    }

    /**
     * Hook: Customize package entity before initial save
     * Can be overridden for custom initialization
     */
    protected void beforeSavePackage(Package packageEntity, CreatePackageRequest request) {
        // Default implementation - empty
    }

    /**
     * Hook: Post-processing after package creation
     * Can be overridden for notifications, logging, etc.
     */
    protected void afterSavePackage(Package saved) {
        // Default implementation - empty
    }

    /**
     * Hook: Validate before updating package
     */
    protected void validateUpdatePackage(Package entity, UpdatePackageRequest request, String userId) {
        // Default implementation - empty
    }

    /**
     * Hook: Before updating package
     */
    protected void beforeUpdatePackage(Package entity, UpdatePackageRequest request) {
        // Default implementation - empty
    }

    /**
     * Hook: After updating package
     */
    protected void afterUpdatePackage(Package updated) {
        // Default implementation - empty
    }

    /**
     * Hook: Before deleting package
     */
    protected void beforeDeletePackage(Package entity, String userId) {
        // Default implementation - empty
    }

    /**
     * Hook: After deleting package
     */
    protected void afterDeletePackage(String packageId, String userId) {
        // Default implementation - empty
    }
}
