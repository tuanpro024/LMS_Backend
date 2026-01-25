package com.lms.writing.service.impl;

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

    private final FolderRepository folderRepository;
    private final StudySetRepository studySetRepository;
    private final WordRepository wordRepository;

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
        if (result.studySetsWithWords.isEmpty()) {
            throw new InvalidFileFormatException("No valid study sets found in the file");
        }

        // Save study sets trước
        List<StudySet> savedStudySets = new ArrayList<>();
        for (StudySetWithWords ssw : result.studySetsWithWords) {
            StudySet saved = studySetRepository.save(ssw.studySet);
            savedStudySets.add(saved);

            // Save words for this study set
            for (Word word : ssw.words) {
                word.setStudySet(saved);
                wordRepository.save(word);
            }

            log.debug("Saved study set: {} with {} words", saved.getTitle(), ssw.words.size());
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
                savedFolder.getId(), result.studySetsWithWords.size(), result.totalWords);

        // Build response
        return ExcelImportResponse.builder()
                .folderId(savedFolder.getId())
                .folderName(savedFolder.getName())
                .totalStudySets(result.studySetsWithWords.size())
                .totalWords(result.totalWords)
                .studySetIds(savedFolder.getStudySets().stream()
                        .map(StudySet::getId)
                        .toList())
                .warnings(result.warnings)
                .message(String.format("Successfully imported %d study sets with %d words",
                        result.studySetsWithWords.size(), result.totalWords))
                .build();
    }

    /**
     * Process rows và tạo study sets với words
     */
    private ImportResult processRows(List<ExcelParser.ExcelRow> rows, String userId, boolean isPrivate) {
        List<StudySetWithWords> studySetsWithWords = new ArrayList<>();
        List<ImportWarning> warnings = new ArrayList<>();
        int totalWords = 0;

        StudySet currentStudySet = null;
        List<Word> currentWords = null;

        for (ExcelParser.ExcelRow row : rows) {
            // Skip empty rows
            if (ExcelParser.isRowEmpty(row)) {
                // Dòng trống đánh dấu kết thúc study set hiện tại
                if (currentStudySet != null && currentWords != null && !currentWords.isEmpty()) {
                    studySetsWithWords.add(new StudySetWithWords(currentStudySet, currentWords));
                    currentStudySet = null;
                    currentWords = null;
                }
                continue;
            }

            // Nếu cột A có giá trị → Study Set mới
            if (row.getStudySetName() != null && !row.getStudySetName().trim().isEmpty()) {
                // Save study set cũ nếu có
                if (currentStudySet != null && currentWords != null && !currentWords.isEmpty()) {
                    studySetsWithWords.add(new StudySetWithWords(currentStudySet, currentWords));
                }

                // Tạo study set mới
                currentStudySet = StudySet.builder()
                        .title(row.getStudySetName().trim())
                        .isPrivate(isPrivate)
                        .userId(userId)
                        .build();
                currentWords = new ArrayList<>();

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

                // Tạo word với tất cả các thuộc tính (không set studySet ở đây)
                Word word = new Word();
                word.setWord(termValue);
                word.setMeaning(meaning.trim());
                word.setPinyin(row.getPinyin() != null ? row.getPinyin().trim() : "");
                word.setSinoVn(row.getSinoVn() != null ? row.getSinoVn().trim() : null);
                word.setWordType(row.getWordType() != null ? row.getWordType().trim() : null);
                word.setHskLevel(row.getHskLevel() != null ? row.getHskLevel().trim() : null);
                word.setImageWord(row.getImageWord() != null ? row.getImageWord().trim() : null);
                word.setSinoOrigin(row.getSinoOrigin() != null ? row.getSinoOrigin().trim() : null);
                word.setImageOrigin(row.getImageOrigin() != null ? row.getImageOrigin().trim() : null);
                word.setExample(row.getExampleSentence() != null ? row.getExampleSentence().trim() : null);
                word.setExamplePinyin(row.getExamplePinyin() != null ? row.getExamplePinyin().trim() : null);
                word.setExampleMeaning(row.getExampleMeaning() != null ? row.getExampleMeaning().trim() : null);
                word.setCharacters(charactersJson);

                currentWords.add(word);
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
        if (currentStudySet != null && currentWords != null && !currentWords.isEmpty()) {
            studySetsWithWords.add(new StudySetWithWords(currentStudySet, currentWords));
        }

        return new ImportResult(studySetsWithWords, totalWords, warnings);
    }

    /**
     * Helper class để group StudySet với Words của nó
     */
    private record StudySetWithWords(
            StudySet studySet,
            List<Word> words) {
    }

    /**
     * Helper class để return multiple values
     */
    private record ImportResult(
            List<StudySetWithWords> studySetsWithWords,
            int totalWords,
            List<ImportWarning> warnings) {
    }
}
