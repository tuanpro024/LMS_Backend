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
import java.util.function.Supplier;

/**
 * Abstract base implementation of HierarchicalImportService
 * Subclasses need to implement createContentItem() to create specific item
 * types
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractHierarchicalImportService<T extends ImportableContentItem>
        implements HierarchicalImportService<T> {

    protected final PackageRepository packageRepository;
    protected final SubjectRepository subjectRepository;
    protected final SlotRepository slotRepository;
    protected final FolderRepository folderRepository;
    protected final StudySetRepository studySetRepository;
    protected final TypeRepository typeRepository;

    /**
     * Create a content item (Card, Word, etc.) from import row
     * Subclasses must implement this
     */
    protected abstract T createContentItem(HierarchicalImportRow row, int index);

    /**
     * Save a content item to database
     * Subclasses must implement this
     */
    protected abstract void saveContentItem(T item);

    /**
     * Get content item type name for logging
     */
    protected abstract String getContentItemTypeName();

    @Transactional
    public HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            TypeName typeName,
            String userId,
            boolean isPrivate) {

        log.info("Starting hierarchical Excel import for user: {} with typeName: {}", userId, typeName);

        // Validate typeName
        if (typeName == null) {
            throw new IllegalArgumentException("TypeName is required");
        }

        // Find or create Type by TypeName
        Type type = typeRepository.findByName(typeName)
                .orElseGet(() -> {
                    log.info("Type '{}' not found, creating default Type", typeName);
                    Type newType = Type.builder()
                            .name(typeName)
                            .description("Auto-created from import")
                            .build();
                    return typeRepository.save(newType);
                });

        // Parse file
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

        // Process rows
        HierarchicalImportResult result = processRows(rows, type, userId, isPrivate);

        // Build success message
        result.setMessage(String.format(
                "Successfully imported %d package(s), %d subject(s), %d slot(s), %d folder(s), %d study set(s), %d %s(s)",
                result.getTotalPackages(),
                result.getTotalSubjects(),
                result.getTotalSlots(),
                result.getTotalFolders(),
                result.getTotalStudySets(),
                result.getTotalContentItems(),
                getContentItemTypeName().toLowerCase()));

        log.info("Import completed: {}", result.getMessage());

        return result;
    }

    /**
     * Process all rows and build hierarchy
     */
    private HierarchicalImportResult processRows(
            List<HierarchicalImportRow> rows,
            Type packageType,
            String userId,
            boolean isPrivate) {

        HierarchicalImportResult result = HierarchicalImportResult.builder()
                .packageIds(new ArrayList<>())
                .subjectIds(new ArrayList<>())
                .slotIds(new ArrayList<>())
                .folderIds(new ArrayList<>())
                .studySetIds(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();

        // Current context
        Package currentPackage = null;
        Subject currentSubject = null;
        Slot currentSlot = null;
        Folder currentFolder = null;
        StudySet currentStudySet = null;
        List<T> currentContentItems = null;
        int contentItemIndex = 0;

        for (HierarchicalImportRow row : rows) {
            // Skip empty rows
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
                // Save previous Folder and StudySet hierarchy if exists (Package/Subject/Slot
                // already persisted)
                if (currentFolder != null) {
                    if (currentSlot != null) {
                        currentFolder.setSlot(currentSlot);
                    } else if (currentSubject != null) {
                        currentFolder.setSubject(currentSubject);
                    } else if (currentPackage != null) {
                        currentFolder.setPackageEntity(currentPackage);
                    }
                    saveFolderHierarchy(currentFolder, currentStudySet, currentContentItems, result);
                }

                // Create and immediately persist new package to get ID
                Package newPackage = createPackage(row, packageType, userId, result);
                if (newPackage != null) {
                    currentPackage = packageRepository.save(newPackage);
                    result.getPackageIds().add(currentPackage.getId());
                    result.setTotalPackages(result.getTotalPackages() + 1);
                    log.debug("Persisted package: {}", currentPackage.getName());
                } else {
                    currentPackage = null;
                }

                currentSubject = null;
                currentSlot = null;
                currentFolder = null;
                currentStudySet = null;
                currentContentItems = null;
                contentItemIndex = 0;
            }

            // Subject level
            if (row.hasSubjectData()) {
                // Save previous Slot/Folder hierarchy if exists (Subject already persisted)
                if (currentSlot != null) {
                    saveSlotHierarchy(currentSlot, currentFolder, currentStudySet, currentContentItems, result);
                } else if (currentFolder != null) {
                    if (currentSubject != null) {
                        currentFolder.setSubject(currentSubject);
                    }
                    saveFolderHierarchy(currentFolder, currentStudySet, currentContentItems, result);
                }

                if (currentPackage == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_SUBJECT,
                            "Subject found without a package. Skipped.");
                    continue;
                }

                // Create and immediately persist Subject to get ID
                Subject newSubject = createSubject(row, currentPackage, userId);
                newSubject.setPackageEntity(currentPackage); // currentPackage already persisted with ID
                currentSubject = subjectRepository.save(newSubject);
                currentPackage.addSubject(currentSubject);
                result.getSubjectIds().add(currentSubject.getId());
                result.setTotalSubjects(result.getTotalSubjects() + 1);
                log.debug("Persisted subject: {}", currentSubject.getName());

                currentSlot = null;
                currentFolder = null;
                currentStudySet = null;
                currentContentItems = null;
                contentItemIndex = 0;
            }

            // Slot level
            if (row.hasSlotData()) {
                // Save previous Folder hierarchy if exists (Slot already persisted)
                if (currentFolder != null) {
                    if (currentSlot != null) {
                        currentFolder.setSlot(currentSlot);
                    }
                    saveFolderHierarchy(currentFolder, currentStudySet, currentContentItems, result);
                }

                if (currentSubject == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_SLOT,
                            "Slot found without a subject. Skipped.");
                    continue;
                }

                // Create and immediately persist Slot to get ID
                Slot newSlot = createSlot(row, currentSubject, userId);
                newSlot.setSubject(currentSubject); // currentSubject already persisted with ID
                currentSlot = slotRepository.save(newSlot);
                currentSubject.addSlot(currentSlot);
                result.getSlotIds().add(currentSlot.getId());
                result.setTotalSlots(result.getTotalSlots() + 1);
                log.debug("Persisted slot: {}", currentSlot.getName());

                currentFolder = null;
                currentStudySet = null;
                currentContentItems = null;
                contentItemIndex = 0;
            }

            // Folder level
            if (row.hasFolderData()) {
                // Save previous folder hierarchy if exists
                if (currentFolder != null) {
                    // Set appropriate parent (all already persisted)
                    if (currentSlot != null) {
                        currentFolder.setSlot(currentSlot);
                    } else if (currentSubject != null) {
                        currentFolder.setSubject(currentSubject);
                    } else if (currentPackage != null) {
                        currentFolder.setPackageEntity(currentPackage);
                    }
                    saveFolderHierarchy(currentFolder, currentStudySet, currentContentItems, result);
                }

                // Folder can belong to Package (no subject/slot), Subject (no slot), or Slot
                if (currentPackage == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_FOLDER,
                            "Folder found without a package. Skipped.");
                    continue;
                }

                currentFolder = createFolder(row, currentPackage, currentSubject, currentSlot, userId, isPrivate);
                currentStudySet = null;
                currentContentItems = null;
                contentItemIndex = 0;

                result.setTotalFolders(result.getTotalFolders() + 1);
            }

            // StudySet level
            if (row.hasStudySetData()) {
                // Save previous study set if exists
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

                currentStudySet = createStudySet(row, userId, isPrivate);
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

        // Save remaining Folder and StudySet hierarchy (Package/Subject/Slot already
        // persisted)
        if (currentFolder != null) {
            // Set appropriate parent (all already persisted with ID)
            if (currentSlot != null) {
                currentFolder.setSlot(currentSlot);
            } else if (currentSubject != null) {
                currentFolder.setSubject(currentSubject);
            } else if (currentPackage != null) {
                currentFolder.setPackageEntity(currentPackage);
            }
            saveFolderHierarchy(currentFolder, currentStudySet, currentContentItems, result);
        } else if (currentStudySet != null && currentContentItems != null && !currentContentItems.isEmpty()) {
            // Orphan StudySet without Folder - should not happen but handle it
            saveStudySetWithItems(currentStudySet, currentContentItems);
            result.setTotalStudySets(result.getTotalStudySets() + 1);
            result.setTotalContentItems(result.getTotalContentItems() + currentContentItems.size());
        }

        return result;
    }

    /**
     * Create Package entity
     */
    private Package createPackage(HierarchicalImportRow row, Type packageType, String userId,
            HierarchicalImportResult result) {
        return Package.builder()
                .name(row.getPackageName().trim())
                .description(row.getPackageDescription() != null ? row.getPackageDescription().trim() : null)
                .type(packageType)
                .userId(userId)
                .subjects(new ArrayList<>())
                .folders(new ArrayList<>())
                .build();
    }

    /**
     * Create Subject entity (packageEntity will be set later after Package is
     * persisted)
     */
    private Subject createSubject(HierarchicalImportRow row, Package packageEntity, String userId) {
        return Subject.builder()
                .name(row.getSubjectName().trim())
                .code(row.getSubjectCode() != null ? row.getSubjectCode().trim() : "")
                .description(row.getSubjectDescription() != null ? row.getSubjectDescription().trim() : null)
                .userId(userId)
                .slots(new ArrayList<>())
                .folders(new ArrayList<>())
                .build();
    }

    /**
     * Create Slot entity (subject will be set later after Subject is persisted)
     */
    private Slot createSlot(HierarchicalImportRow row, Subject subject, String userId) {
        return Slot.builder()
                .name(row.getSlotName().trim())
                .slotNumber(row.getSlotNumber() != null ? row.getSlotNumber().trim() : null)
                .description(row.getSlotDescription() != null ? row.getSlotDescription().trim() : null)
                .userId(userId)
                .folders(new ArrayList<>())
                .build();
    }

    /**
     * Create Folder entity
     */
    /**
     * Create Folder entity (parent references will be set later after they are
     * persisted)
     */
    private Folder createFolder(HierarchicalImportRow row, Package packageEntity,
            Subject subject, Slot slot, String userId, boolean isPrivate) {
        return Folder.builder()
                .name(row.getFolderName().trim())
                .description(row.getFolderDescription() != null ? row.getFolderDescription().trim() : null)
                .userId(userId)
                .isPrivate(isPrivate)
                .studySets(new ArrayList<>())
                .build();
    }

    /**
     * Create StudySet entity
     */
    private StudySet createStudySet(HierarchicalImportRow row, String userId, boolean isPrivate) {
        return StudySet.builder()
                .title(row.getStudySetName().trim())
                .userId(userId)
                .isPrivate(isPrivate)
                .build();
    }

    /**
     * Save study set with its content items
     */
    private void saveStudySetWithItems(StudySet studySet, List<T> contentItems) {
        StudySet savedStudySet = studySetRepository.save(studySet);

        for (T item : contentItems) {
            item.setStudySet(savedStudySet);
            saveContentItem(item);
        }

        log.debug("Saved study set: {} with {} {}(s)",
                savedStudySet.getTitle(), contentItems.size(), getContentItemTypeName().toLowerCase());
    }

    /**
     * Save complete package hierarchy
     */
    private void savePackageHierarchy(Package packageEntity, Subject subject, Slot slot,
            Folder folder, StudySet studySet, List<T> contentItems,
            HierarchicalImportResult result) {
        // Save Package first to get ID
        Package savedPackage = packageRepository.save(packageEntity);
        result.getPackageIds().add(savedPackage.getId());
        log.debug("Saved package: {}", savedPackage.getName());

        // Then save children with reference to saved Package (update to persisted
        // entity)
        if (subject != null) {
            subject.setPackageEntity(savedPackage); // Update with persisted Package
            saveSubjectHierarchy(subject, slot, folder, studySet, contentItems, result);
            savedPackage.addSubject(subject);
        } else if (folder != null) {
            folder.setPackageEntity(savedPackage); // Update with persisted Package
            saveFolderHierarchy(folder, studySet, contentItems, result);
            savedPackage.addFolder(folder);
        }
    }

    /**
     * Save complete subject hierarchy
     */
    private void saveSubjectHierarchy(Subject subject, Slot slot, Folder folder,
            StudySet studySet, List<T> contentItems,
            HierarchicalImportResult result) {
        // Save Subject first to get ID
        Subject savedSubject = subjectRepository.save(subject);
        result.getSubjectIds().add(savedSubject.getId());
        log.debug("Saved subject: {}", savedSubject.getName());

        // Then save children with reference to saved Subject (update to persisted
        // entity)
        if (slot != null) {
            slot.setSubject(savedSubject); // Update with persisted Subject
            saveSlotHierarchy(slot, folder, studySet, contentItems, result);
            savedSubject.addSlot(slot);
        } else if (folder != null) {
            folder.setSubject(savedSubject); // Update with persisted Subject
            saveFolderHierarchy(folder, studySet, contentItems, result);
            savedSubject.addFolder(folder);
        }
    }

    /**
     * Save complete slot hierarchy
     */
    private void saveSlotHierarchy(Slot slot, Folder folder, StudySet studySet,
            List<T> contentItems, HierarchicalImportResult result) {
        // Save Slot first to get ID
        Slot savedSlot = slotRepository.save(slot);
        result.getSlotIds().add(savedSlot.getId());
        log.debug("Saved slot: {}", savedSlot.getName());

        // Then save children with reference to saved Slot (update to persisted entity)
        if (folder != null) {
            folder.setSlot(savedSlot); // Update with persisted Slot
            saveFolderHierarchy(folder, studySet, contentItems, result);
            savedSlot.addFolder(folder);
        }
    }

    /**
     * Save complete folder hierarchy
     */
    private void saveFolderHierarchy(Folder folder, StudySet studySet,
            List<T> contentItems, HierarchicalImportResult result) {
        // Save Folder first to get ID
        Folder savedFolder = folderRepository.save(folder);
        result.getFolderIds().add(savedFolder.getId());
        log.debug("Saved folder: {}", savedFolder.getName());

        // Then save children with reference to saved Folder
        if (studySet != null && contentItems != null && !contentItems.isEmpty()) {
            saveStudySetWithItems(studySet, contentItems);
            savedFolder.addStudySet(studySet);
            result.getStudySetIds().add(studySet.getId());
            result.setTotalStudySets(result.getTotalStudySets() + 1);
            result.setTotalContentItems(result.getTotalContentItems() + contentItems.size());
        }
    }
}
