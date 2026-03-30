package com.lms.content.common.service.impl;

import com.lms.content.common.dto.excel.*;
import com.lms.content.common.entity.*;
import com.lms.content.common.entity.Package;
import com.lms.content.common.repository.*;
import com.lms.content.common.service.HierarchicalImportService;
import com.lms.content.common.util.HierarchicalExcelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Abstract base implementation of HierarchicalImportService.
 * Hierarchy: Package -&gt; Folder -&gt; StudySet -&gt; ContentItem
 * Subclasses implement createContentItem() to create specific item types.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractHierarchicalImportService<T extends ImportableContentItem>
        implements HierarchicalImportService<T> {

    protected final PackageRepository packageRepository;
    protected final FolderRepository folderRepository;
    protected final StudySetRepository studySetRepository;
    protected final TypeRepository typeRepository;

    protected abstract T createContentItem(HierarchicalImportRow row, int index);

    protected abstract void saveContentItem(T item);

    protected abstract String getContentItemTypeName();

    @Transactional
    public HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            TypeName typeName,
            String userId,
            boolean isPrivate) {

        log.info("Starting hierarchical Excel import for user: {} with typeName: {}", userId, typeName);

        if (typeName == null) {
            throw new IllegalArgumentException("TypeName is required");
        }

        Type type = typeRepository.findByName(typeName)
                .orElseGet(() -> {
                    log.info("Type '{}' not found, creating default Type", typeName);
                    Type newType = Type.builder()
                            .name(typeName)
                            .description("Auto-created from import")
                            .build();
                    return typeRepository.save(newType);
                });

        List<HierarchicalImportRow> rows;
        try {
            rows = HierarchicalExcelParser.parseFile(file);
        } catch (IOException e) {
            log.error("Failed to parse Excel file: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("File is empty or contains no data");
        }

        HierarchicalImportResult result = processRows(rows, type, userId, isPrivate);

        result.setMessage(String.format(
                "Successfully imported %d package(s), %d folder(s), %d study set(s), %d %s(s)",
                result.getTotalPackages(),
                result.getTotalFolders(),
                result.getTotalStudySets(),
                result.getTotalContentItems(),
                getContentItemTypeName().toLowerCase()));

        log.info("Import completed: {}", result.getMessage());
        return result;
    }

    private HierarchicalImportResult processRows(
            List<HierarchicalImportRow> rows,
            Type packageType,
            String userId,
            boolean isPrivate) {

        HierarchicalImportResult result = HierarchicalImportResult.builder()
                .packageIds(new ArrayList<>())
                .folderIds(new ArrayList<>())
                .studySetIds(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();

        Package currentPackage = null;
        Folder currentFolder = null;
        StudySet currentStudySet = null;
        List<T> currentContentItems = null;
        int contentItemIndex = 0;

        for (HierarchicalImportRow row : rows) {
            if (row.isEmpty()) {
                // Empty row marks end of current study set
                if (currentStudySet != null && currentContentItems != null && !currentContentItems.isEmpty()) {
                    saveStudySetWithItems(currentStudySet, currentContentItems);
                    result.setTotalStudySets(result.getTotalStudySets() + 1);
                    result.setTotalContentItems(result.getTotalContentItems() + currentContentItems.size());
                    currentStudySet = null;
                    currentContentItems = null;
                    contentItemIndex = 0;
                }
                continue;
            }

            // Package level
            if (row.hasPackageData()) {
                if (currentFolder != null) {
                    saveFolderHierarchy(currentFolder, currentStudySet, currentContentItems, result);
                }

                Package newPackage = Package.builder()
                        .name(row.getPackageName().trim())
                        .description(row.getPackageDescription() != null ? row.getPackageDescription().trim() : null)
                        .type(packageType)
                        .userId(userId)
                        .folders(new ArrayList<>())
                        .build();
                currentPackage = packageRepository.save(newPackage);
                result.getPackageIds().add(currentPackage.getId());
                result.setTotalPackages(result.getTotalPackages() + 1);
                log.debug("Persisted package: {}", currentPackage.getName());

                currentFolder = null;
                currentStudySet = null;
                currentContentItems = null;
                contentItemIndex = 0;
            }

            // Folder level
            if (row.hasFolderData()) {
                if (currentFolder != null) {
                    saveFolderHierarchy(currentFolder, currentStudySet, currentContentItems, result);
                }

                if (currentPackage == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_FOLDER,
                            "Folder found without a package. Skipped.");
                    continue;
                }

                currentFolder = Folder.builder()
                        .name(row.getFolderName().trim())
                        .description(row.getFolderDescription() != null ? row.getFolderDescription().trim() : null)
                        .userId(userId)
                        .isPrivate(isPrivate)
                        .studySets(new ArrayList<>())
                        .build();
                currentFolder.setPackageEntity(currentPackage);

                currentStudySet = null;
                currentContentItems = null;
                contentItemIndex = 0;
                result.setTotalFolders(result.getTotalFolders() + 1);
            }

            // StudySet level
            if (row.hasStudySetData()) {
                if (currentStudySet != null && currentContentItems != null && !currentContentItems.isEmpty()) {
                    saveStudySetWithItems(currentStudySet, currentContentItems);
                    result.setTotalStudySets(result.getTotalStudySets() + 1);
                    result.setTotalContentItems(result.getTotalContentItems() + currentContentItems.size());
                }

                if (currentFolder == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_STUDY_SET,
                            "Study set found without a folder. Skipped.");
                    continue;
                }

                currentStudySet = StudySet.builder()
                        .title(row.getStudySetName().trim())
                        .description(row.getStudySetDescription() != null ? row.getStudySetDescription().trim() : null)
                        .userId(userId)
                        .isPrivate(isPrivate)
                        .build();
                currentContentItems = new ArrayList<>();
                contentItemIndex = 0;
            }

            // Content item level
            if (row.hasContentItemData()) {
                if (currentStudySet == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_CONTENT_ITEM,
                            getContentItemTypeName() + " found without a study set. Skipped.");
                    continue;
                }

                if (row.getDefinition() == null || row.getDefinition().trim().isEmpty()) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.MISSING_DEFINITION,
                            getContentItemTypeName() + " has no definition. Using empty string.");
                }

                T contentItem = createContentItem(row, contentItemIndex++);
                currentContentItems.add(contentItem);
            }
        }

        // Save remaining folder/studySet hierarchy
        if (currentFolder != null) {
            saveFolderHierarchy(currentFolder, currentStudySet, currentContentItems, result);
        } else if (currentStudySet != null && currentContentItems != null && !currentContentItems.isEmpty()) {
            saveStudySetWithItems(currentStudySet, currentContentItems);
            result.setTotalStudySets(result.getTotalStudySets() + 1);
            result.setTotalContentItems(result.getTotalContentItems() + currentContentItems.size());
        }

        return result;
    }

    private void saveStudySetWithItems(StudySet studySet, List<T> contentItems) {
        StudySet savedStudySet = studySetRepository.save(studySet);
        for (T item : contentItems) {
            item.setStudySet(savedStudySet);
            saveContentItem(item);
        }
        log.debug("Saved study set: {} with {} {}(s)",
                savedStudySet.getTitle(), contentItems.size(), getContentItemTypeName().toLowerCase());
    }

    private void saveFolderHierarchy(Folder folder, StudySet studySet,
            List<T> contentItems, HierarchicalImportResult result) {
        Folder savedFolder = folderRepository.save(folder);
        result.getFolderIds().add(savedFolder.getId());
        log.debug("Saved folder: {}", savedFolder.getName());

        if (studySet != null && contentItems != null && !contentItems.isEmpty()) {
            saveStudySetWithItems(studySet, contentItems);
            savedFolder.addStudySet(studySet);
            result.getStudySetIds().add(studySet.getId());
            result.setTotalStudySets(result.getTotalStudySets() + 1);
            result.setTotalContentItems(result.getTotalContentItems() + contentItems.size());
        }
    }
}
