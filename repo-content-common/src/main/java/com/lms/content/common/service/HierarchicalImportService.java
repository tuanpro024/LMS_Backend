package com.lms.content.common.service;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.excel.ImportableContentItem;
import com.lms.content.common.entity.TypeName;
import org.springframework.web.multipart.MultipartFile;

/**
 * Generic service for hierarchical Excel import.
 * Hierarchy: Package -&gt; Folder -&gt; StudySet -&gt; ContentItems
 *
 * @param <T> The type of content item (Card, Word, etc.)
 */
public interface HierarchicalImportService<T extends ImportableContentItem> {

    /**
     * Import data from Excel file with hierarchy:
     * Package -&gt; Folder -&gt; StudySet -&gt; ContentItems
     *
     * @param file      Excel file
     * @param typeName  TypeName enum for the Package Type
     * @param userId    User performing the import
     * @param isPrivate Whether created items should be private
     * @return Import result with statistics and warnings
     */
    HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            TypeName typeName,
            String userId,
            boolean isPrivate);
}
