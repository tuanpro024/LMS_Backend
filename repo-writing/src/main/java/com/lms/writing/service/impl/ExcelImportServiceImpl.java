package com.lms.writing.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.writing.service.ExcelImportService;
import com.lms.writing.util.CharacterUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {

    private final WritingHierarchicalImportService hierarchicalImportService;

    @Override
    @Transactional
    public HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            com.lms.content.common.entity.TypeName typeName,
            String userId,
            boolean isPrivate) {
        log.info("Starting hierarchical Excel import for user: {} with typeName: {}", userId, typeName);

        return hierarchicalImportService.importFromPackageExcel(file, typeName, userId, isPrivate);
    }

}
