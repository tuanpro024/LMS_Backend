package com.lms.flashcard.service.impl;

import com.lms.flashcard.dto.response.ExcelImportResponse;
import com.lms.flashcard.dto.response.ImportWarning;
import com.lms.flashcard.entity.Card;
import com.lms.flashcard.entity.Folder;
import com.lms.flashcard.entity.StudySet;
import com.lms.flashcard.exception.InvalidFileFormatException;
import com.lms.flashcard.repository.FolderRepository;
import com.lms.flashcard.repository.StudySetRepository;
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

    private final FolderRepository folderRepository;
    private final StudySetRepository studySetRepository;

    @Override
    @Transactional
    public ExcelImportResponse importFromExcel(
            MultipartFile file,
            String folderName,
            String description,
            boolean isPrivate,
            String userId) {
        log.info("Starting Excel import for user: {}, folder: {}", userId, folderName);

        // Parse file
        List<ExcelParser.ExcelRow> rows;
        try {
            rows = ExcelParser.parseFile(file);
        } catch (InvalidFileFormatException e) {
            log.error("Invalid file format: {}", e.getMessage());
            throw e;
        }

        // Validate có dữ liệu không
        if (rows.isEmpty()) {
            throw new InvalidFileFormatException("File is empty or contains no data");
        }

        // Process rows và tạo entities
        ImportResult result = processRows(rows, userId, isPrivate);

        // Validate có study sets không
        if (result.studySets.isEmpty()) {
            throw new InvalidFileFormatException("No valid study sets found in the file");
        }

        // Save study sets trước (vì ManyToMany không có cascade)
        List<StudySet> savedStudySets = new ArrayList<>();
        for (StudySet studySet : result.studySets) {
            StudySet saved = studySetRepository.save(studySet);
            savedStudySets.add(saved);
            log.debug("Saved study set: {} with {} cards", saved.getTitle(), saved.getCards().size());
        }

        // Tạo folder
        Folder folder = Folder.builder()
                .name(folderName)
                .description(description)
                .isPrivate(isPrivate)
                .userId(userId)
                .build();

        // Add study sets vào folder
        for (StudySet studySet : savedStudySets) {
            folder.addStudySet(studySet);
        }

        // Save folder
        Folder savedFolder = folderRepository.save(folder);

        log.info("Import completed. Folder ID: {}, Study Sets: {}, Cards: {}",
                savedFolder.getId(), result.studySets.size(), result.totalCards);

        // Build response
        return ExcelImportResponse.builder()
                .folderId(savedFolder.getId())
                .folderName(savedFolder.getName())
                .totalStudySets(result.studySets.size())
                .totalCards(result.totalCards)
                .studySetIds(savedFolder.getStudySets().stream()
                        .map(StudySet::getId)
                        .toList())
                .warnings(result.warnings)
                .message(String.format("Successfully imported %d study sets with %d cards",
                        result.studySets.size(), result.totalCards))
                .build();
    }

    /**
     * Process rows và tạo study sets với cards
     */
    private ImportResult processRows(List<ExcelParser.ExcelRow> rows, String userId, boolean isPrivate) {
        List<StudySet> studySets = new ArrayList<>();
        List<ImportWarning> warnings = new ArrayList<>();
        int totalCards = 0;

        StudySet currentStudySet = null;
        int cardIndex = 0;

        for (ExcelParser.ExcelRow row : rows) {
            // Skip empty rows
            if (ExcelParser.isRowEmpty(row)) {
                // Dòng trống đánh dấu kết thúc study set hiện tại
                if (currentStudySet != null && !currentStudySet.getCards().isEmpty()) {
                    studySets.add(currentStudySet);
                    currentStudySet = null;
                    cardIndex = 0;
                }
                continue;
            }

            // Nếu cột A có giá trị → Study Set mới
            if (row.getStudySetName() != null && !row.getStudySetName().trim().isEmpty()) {
                // Save study set cũ nếu có
                if (currentStudySet != null && !currentStudySet.getCards().isEmpty()) {
                    studySets.add(currentStudySet);
                }

                // Tạo study set mới
                currentStudySet = StudySet.builder()
                        .title(row.getStudySetName().trim())
                        .isPrivate(isPrivate)
                        .userId(userId)
                        .build();
                cardIndex = 0;

                log.debug("Created new study set: {}", row.getStudySetName());
            }

            // Nếu có term (cột B) → tạo card
            if (row.getTerm() != null && !row.getTerm().trim().isEmpty()) {
                if (currentStudySet == null) {
                    // Card không thuộc study set nào
                    warnings.add(ImportWarning.builder()
                            .rowNumber(row.getRowNumber())
                            .type(ImportWarning.WarningType.MISSING_STUDY_SET_NAME)
                            .message("Card found without a study set name. Skipped.")
                            .build());
                    continue;
                }

                // Validate definition
                String definition = row.getDefinition();
                if (definition == null || definition.trim().isEmpty()) {
                    warnings.add(ImportWarning.builder()
                            .rowNumber(row.getRowNumber())
                            .type(ImportWarning.WarningType.MISSING_DEFINITION)
                            .message("Card has no definition. Using empty string.")
                            .build());
                    definition = "";
                }

                // Tạo card
                Card card = Card.builder()
                        .term(row.getTerm().trim())
                        .definition(definition.trim())
                        .cardIndex(cardIndex++)
                        .imageUrl(row.getImageFileName() != null ? row.getImageFileName().trim() : null)
                        .pinyin(row.getPinyin() != null ? row.getPinyin().trim() : null)
                        .pronunciation(row.getPronunciation() != null ? row.getPronunciation().trim() : null)
                        .exampleSentence(row.getExampleSentence() != null ? row.getExampleSentence().trim() : null)
                        .build();

                currentStudySet.addCard(card);
                totalCards++;

                log.debug("Added card to study set: {} - {}", row.getTerm(), currentStudySet.getTitle());
            } else if (row.getDefinition() != null && !row.getDefinition().trim().isEmpty()) {
                // Có definition nhưng không có term
                warnings.add(ImportWarning.builder()
                        .rowNumber(row.getRowNumber())
                        .type(ImportWarning.WarningType.MISSING_TERM)
                        .message("Row has definition but no term. Skipped.")
                        .build());
            }
        }

        // Lưu study set cuối cùng nếu có
        if (currentStudySet != null && !currentStudySet.getCards().isEmpty()) {
            studySets.add(currentStudySet);
        }

        return new ImportResult(studySets, totalCards, warnings);
    }

    /**
     * Helper class để return multiple values
     */
    private record ImportResult(
            List<StudySet> studySets,
            int totalCards,
            List<ImportWarning> warnings) {
    }
}
