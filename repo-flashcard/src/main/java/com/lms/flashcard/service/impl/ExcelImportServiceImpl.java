package com.lms.flashcard.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.flashcard.dto.response.ExcelImportResponse;
import com.lms.flashcard.dto.response.ImportWarning;
import com.lms.flashcard.entity.Card;
import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.StudySet;
import com.lms.flashcard.exception.InvalidFileFormatException;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.flashcard.service.ExcelImportService;
import com.lms.flashcard.util.ExcelParser;
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
    private final FlashcardHierarchicalImportService hierarchicalImportService;

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
