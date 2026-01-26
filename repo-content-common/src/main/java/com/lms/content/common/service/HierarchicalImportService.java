package com.lms.content.common.service;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.excel.ImportableContentItem;
import org.springframework.web.multipart.MultipartFile;

/**
 * Generic service for hierarchical Excel import
 * Can be used by both flashcard and writing modules
 * 
 * @param <T> The type of content item (Card, Word, etc.)
 */
public interface HierarchicalImportService<T extends ImportableContentItem> {

    /**
     * Import data from Excel file with full hierarchy:
     * Package -> Subject -> Slot -> Folder -> StudySet -> ContentItems
     * 
     * @param file          Excel file
     * @param packageTypeId ID of the Package Type (all packages will have this type)
     * @param userId        User performing the import
     * @param isPrivate     Whether created items should be private
     * @return Import result with statistics and warnings
     */
    HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            String packageTypeId,
            String userId,
            boolean isPrivate);
}
