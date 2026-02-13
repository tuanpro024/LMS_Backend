package com.lms.pronunciation.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.pronunciation.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {
    private final PronunciationHierarchicalImportService hierarchicalImportService;

    @Override
    @Transactional
    public HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            String packageTypeId,
            String userId,
            boolean isPrivate) {
        log.info("Starting hierarchical Excel import for user: {} with packageTypeId: {}", userId, packageTypeId);

        return hierarchicalImportService.importFromPackageExcel(file, packageTypeId, userId, isPrivate);
    }
}
