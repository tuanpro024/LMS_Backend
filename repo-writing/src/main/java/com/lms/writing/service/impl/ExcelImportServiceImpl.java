package com.lms.writing.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.writing.dto.response.ExcelImportResponse;
import com.lms.writing.dto.response.ImportWarning;
import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.StudySet;
import com.lms.writing.entity.Word;
import com.lms.writing.exception.InvalidFileFormatException;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.writing.repository.WordRepository;
import com.lms.writing.service.ExcelImportService;
import com.lms.writing.util.CharacterUtils;
import com.lms.writing.util.ExcelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {

    private final WritingHierarchicalImportService hierarchicalImportService;

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
