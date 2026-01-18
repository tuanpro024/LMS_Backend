package com.lms.writing.service.impl;

import com.lms.writing.dto.response.ExcelImportResponse;
import com.lms.writing.dto.response.ImportWarning;
import com.lms.writing.entity.Folder;
import com.lms.writing.entity.StudySet;
import com.lms.writing.entity.Word;
import com.lms.writing.exception.InvalidFileFormatException;
import com.lms.writing.repository.FolderRepository;
import com.lms.writing.repository.StudySetRepository;
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
            log.debug("Saved study set: {} with {} words", saved.getTitle(), saved.getWords().size());
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

        log.info("Import completed. Folder ID: {}, Study Sets: {}, Words: {}",
                savedFolder.getId(), result.studySets.size(), result.totalWords);

        // Build response
        return ExcelImportResponse.builder()
                .folderId(savedFolder.getId())
                .folderName(savedFolder.getName())
                .totalStudySets(result.studySets.size())
                .totalWords(result.totalWords)
                .studySetIds(savedFolder.getStudySets().stream()
                        .map(StudySet::getId)
                        .toList())
                .warnings(result.warnings)
                .message(String.format("Successfully imported %d study sets with %d words",
                        result.studySets.size(), result.totalWords))
                .build();
    }

    /**
     * Process rows và tạo study sets với words
     */
    private ImportResult processRows(List<ExcelParser.ExcelRow> rows, String userId, boolean isPrivate) {
        List<StudySet> studySets = new ArrayList<>();
        List<ImportWarning> warnings = new ArrayList<>();
        int totalWords = 0;

        StudySet currentStudySet = null;
        int wordIndex = 0;

        for (ExcelParser.ExcelRow row : rows) {
            // Skip empty rows
            if (ExcelParser.isRowEmpty(row)) {
                // Dòng trống đánh dấu kết thúc study set hiện tại
                if (currentStudySet != null && !currentStudySet.getWords().isEmpty()) {
                    studySets.add(currentStudySet);
                    currentStudySet = null;
                    wordIndex = 0;
                }
                continue;
            }

            // Nếu cột A có giá trị → Study Set mới
            if (row.getStudySetName() != null && !row.getStudySetName().trim().isEmpty()) {
                // Save study set cũ nếu có
                if (currentStudySet != null && !currentStudySet.getWords().isEmpty()) {
                    studySets.add(currentStudySet);
                }

                // Tạo study set mới
                currentStudySet = StudySet.builder()
                        .title(row.getStudySetName().trim())
                        .isPrivate(isPrivate)
                        .userId(userId)
                        .build();
                wordIndex = 0;

                log.debug("Created new study set: {}", row.getStudySetName());
            }

            // Nếu có term (cột B) → tạo word
            if (row.getTerm() != null && !row.getTerm().trim().isEmpty()) {
                if (currentStudySet == null) {
                    // Word không thuộc study set nào
                    warnings.add(ImportWarning.builder()
                            .rowNumber(row.getRowNumber())
                            .type(ImportWarning.WarningType.MISSING_STUDY_SET_NAME)
                            .message("Word found without a study set name. Skipped.")
                            .build());
                    continue;
                }

                // Validate definition/meaning
                String meaning = row.getDefinition();
                if (meaning == null || meaning.trim().isEmpty()) {
                    warnings.add(ImportWarning.builder()
                            .rowNumber(row.getRowNumber())
                            .type(ImportWarning.WarningType.MISSING_DEFINITION)
                            .message("Word has no meaning. Using empty string.")
                            .build());
                    meaning = "";
                }

                // Generate characters JSON if not provided
                String termValue = row.getTerm().trim();
                String charactersJson = row.getCharactersJson();
                if (charactersJson == null || charactersJson.trim().isEmpty()) {
                    charactersJson = CharacterUtils.wordToJsonArray(termValue);
                }

                // Tạo word với tất cả các thuộc tính
                Word word = Word.builder()
                        .word(termValue)
                        .meaning(meaning.trim())
                        .wordIndex(wordIndex++)
                        .pinyin(row.getPinyin() != null ? row.getPinyin().trim() : "")
                        .sinoVn(row.getSinoVn() != null ? row.getSinoVn().trim() : null)
                        .wordType(row.getWordType() != null ? row.getWordType().trim() : null)
                        .hskLevel(row.getHskLevel() != null ? row.getHskLevel().trim() : null)
                        .imageWord(row.getImageWord() != null ? row.getImageWord().trim() : null)
                        .sinoOrigin(row.getSinoOrigin() != null ? row.getSinoOrigin().trim() : null)
                        .imageOrigin(row.getImageOrigin() != null ? row.getImageOrigin().trim() : null)
                        .example(row.getExampleSentence() != null ? row.getExampleSentence().trim() : null)
                        .examplePinyin(row.getExamplePinyin() != null ? row.getExamplePinyin().trim() : null)
                        .exampleMeaning(row.getExampleMeaning() != null ? row.getExampleMeaning().trim() : null)
                        .characters(charactersJson)
                        .build();

                currentStudySet.addWord(word);
                totalWords++;

                log.debug("Added word to study set: {} - {}", row.getTerm(), currentStudySet.getTitle());
            } else if (row.getDefinition() != null && !row.getDefinition().trim().isEmpty()) {
                // Có definition nhưng không có term
                warnings.add(ImportWarning.builder()
                        .rowNumber(row.getRowNumber())
                        .type(ImportWarning.WarningType.MISSING_TERM)
                        .message("Row has meaning but no word. Skipped.")
                        .build());
            }
        }

        // Lưu study set cuối cùng nếu có
        if (currentStudySet != null && !currentStudySet.getWords().isEmpty()) {
            studySets.add(currentStudySet);
        }

        return new ImportResult(studySets, totalWords, warnings);
    }

    /**
     * Helper class để return multiple values
     */
    private record ImportResult(
            List<StudySet> studySets,
            int totalWords,
            List<ImportWarning> warnings) {
    }
}
