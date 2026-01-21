package com.lms.flashcard.service.impl;

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

    private final FolderRepository folderRepository;
    private final StudySetRepository studySetRepository;
    private final com.lms.flashcard.repository.CardRepository cardRepository;

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
        if (result.studySetsWithCards.isEmpty()) {
            throw new InvalidFileFormatException("No valid study sets found in the file");
        }

        // Save study sets trước
        List<StudySet> savedStudySets = new ArrayList<>();
        for (StudySetWithCards ssc : result.studySetsWithCards) {
            StudySet saved = studySetRepository.save(ssc.studySet);
            savedStudySets.add(saved);

            // Save cards for this study set
            for (Card card : ssc.cards) {
                card.setStudySet(saved);
                cardRepository.save(card);
            }

            log.debug("Saved study set: {} with {} cards", saved.getTitle(), ssc.cards.size());
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
                savedFolder.getId(), result.studySetsWithCards.size(), result.totalCards);

        // Build response
        return ExcelImportResponse.builder()
                .folderId(savedFolder.getId())
                .folderName(savedFolder.getName())
                .totalStudySets(result.studySetsWithCards.size())
                .totalCards(result.totalCards)
                .studySetIds(savedFolder.getStudySets().stream()
                        .map(StudySet::getId)
                        .toList())
                .warnings(result.warnings)
                .message(String.format("Successfully imported %d study sets with %d cards",
                        result.studySetsWithCards.size(), result.totalCards))
                .build();
    }

    /**
     * Process rows và tạo study sets với cards
     */
    private ImportResult processRows(List<ExcelParser.ExcelRow> rows, String userId, boolean isPrivate) {
        List<StudySetWithCards> studySetsWithCards = new ArrayList<>();
        List<ImportWarning> warnings = new ArrayList<>();
        int totalCards = 0;

        StudySet currentStudySet = null;
        List<Card> currentCards = null;
        int contentIndex = 0;

        for (ExcelParser.ExcelRow row : rows) {
            // Skip empty rows
            if (ExcelParser.isRowEmpty(row)) {
                // Dòng trống đánh dấu kết thúc study set hiện tại
                if (currentStudySet != null && currentCards != null && !currentCards.isEmpty()) {
                    studySetsWithCards.add(new StudySetWithCards(currentStudySet, currentCards));
                    currentStudySet = null;
                    currentCards = null;
                    contentIndex = 0;
                }
                continue;
            }

            // Nếu cột A có giá trị → Study Set mới
            if (row.getStudySetName() != null && !row.getStudySetName().trim().isEmpty()) {
                // Save study set cũ nếu có
                if (currentStudySet != null && currentCards != null && !currentCards.isEmpty()) {
                    studySetsWithCards.add(new StudySetWithCards(currentStudySet, currentCards));
                }

                // Tạo study set mới
                currentStudySet = StudySet.builder()
                        .title(row.getStudySetName().trim())
                        .isPrivate(isPrivate)
                        .userId(userId)
                        .build();
                currentCards = new ArrayList<>();
                contentIndex = 0;

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

                // Tạo card với tất cả các thuộc tính (không set studySet ở đây)
                Card card = new Card();
                card.setTerm(row.getTerm().trim());
                card.setDefinition(definition.trim());
                card.setContentIndex(contentIndex++);
                card.setPinyin(row.getPinyin() != null ? row.getPinyin().trim() : null);
                card.setSinoVn(row.getSinoVn() != null ? row.getSinoVn().trim() : null);
                card.setWordType(row.getWordType() != null ? row.getWordType().trim() : null);
                card.setHskLevel(row.getHskLevel() != null ? row.getHskLevel().trim() : null);
                card.setImageWord(row.getImageWord() != null ? row.getImageWord().trim() : null);
                card.setSinoOrigin(row.getSinoOrigin() != null ? row.getSinoOrigin().trim() : null);
                card.setImageOrigin(row.getImageOrigin() != null ? row.getImageOrigin().trim() : null);
                card.setExampleSentence(row.getExampleSentence() != null ? row.getExampleSentence().trim() : null);
                card.setExamplePinyin(row.getExamplePinyin() != null ? row.getExamplePinyin().trim() : null);
                card.setExampleMeaning(row.getExampleMeaning() != null ? row.getExampleMeaning().trim() : null);
                card.setCharacters(row.getCharactersJson() != null ? row.getCharactersJson().trim() : null);

                currentCards.add(card);
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
        if (currentStudySet != null && currentCards != null && !currentCards.isEmpty()) {
            studySetsWithCards.add(new StudySetWithCards(currentStudySet, currentCards));
        }

        return new ImportResult(studySetsWithCards, totalCards, warnings);
    }

    /**
     * Helper class để group StudySet với Cards của nó
     */
    private record StudySetWithCards(
            StudySet studySet,
            List<Card> cards) {
    }

    /**
     * Helper class để return multiple values
     */
    private record ImportResult(
            List<StudySetWithCards> studySetsWithCards,
            int totalCards,
            List<ImportWarning> warnings) {
    }
}
