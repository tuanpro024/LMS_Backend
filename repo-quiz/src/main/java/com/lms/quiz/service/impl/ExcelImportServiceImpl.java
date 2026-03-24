package com.lms.quiz.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.excel.ImportWarning;
import com.lms.content.common.entity.*;
import com.lms.content.common.entity.Package;
import com.lms.content.common.repository.*;
import com.lms.quiz.dto.excel.QuizExcelImportRow;
import com.lms.quiz.dto.request.CreateQuestionRequest;
import com.lms.quiz.dto.request.CreateQuizRequest;
import com.lms.quiz.entity.enums.DifficultyLevel;
import com.lms.quiz.entity.enums.QuestionType;
import com.lms.quiz.service.ExcelImportService;
import com.lms.quiz.service.IQuizService;
import com.lms.quiz.service.validation.QuizQuestionImportHelper;
import com.lms.quiz.service.validation.QuizImportValidationService;
import com.lms.quiz.util.QuizExcelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel import service for Quiz module.
 * 
 * Unlike flashcard/writing which use AbstractHierarchicalImportService (simple
 * term-definition),
 * quiz has complex multi-type questions, so we implement a custom import logic
 * that:
 * 1. Builds Package → Subject → Slot → Folder → StudySet hierarchy (same
 * pattern)
 * 2. Parses quiz-specific data (questions with options/blanks/pairs/chunks)
 * 3. Creates quizzes via IQuizService.createQuiz()
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {

    private final PackageRepository packageRepository;
    private final SubjectRepository subjectRepository;
    private final SlotRepository slotRepository;
    private final FolderRepository folderRepository;
    private final StudySetRepository studySetRepository;
    private final TypeRepository typeRepository;
    private final IQuizService quizService;
    private final QuizImportValidationService validationService;
    private final QuizQuestionImportHelper questionImportHelper;

    @Override
    @Transactional
    public HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            TypeName typeName,
            String userId,
            boolean isPrivate) {

        log.info("Starting Quiz Excel import for user: {} with typeName: {}", userId, typeName);

        // Validate typeName
        if (typeName == null) {
            throw new IllegalArgumentException("TypeName is required");
        }

        // Find or create Type by TypeName
        Type type = typeRepository.findByName(typeName)
                .orElseGet(() -> {
                    log.info("Type '{}' not found, creating default Type", typeName);
                    Type newType = Type.builder()
                            .name(typeName)
                            .description("Auto-created from import")
                            .build();
                    return typeRepository.save(newType);
                });

        // Parse file
        List<QuizExcelImportRow> rows;
        try {
            rows = QuizExcelParser.parseFile(file);
        } catch (IOException e) {
            log.error("Failed to parse Excel file: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("File is empty or contains no data");
        }

        // Process rows
        HierarchicalImportResult result = processRows(rows, type, userId, isPrivate);

        // Build success message
        result.setMessage(String.format(
                "Successfully imported %d package(s), %d subject(s), %d slot(s), %d folder(s), %d study set(s), %d quiz(zes)",
                result.getTotalPackages(),
                result.getTotalSubjects(),
                result.getTotalSlots(),
                result.getTotalFolders(),
                result.getTotalStudySets(),
                result.getTotalContentItems()));

        log.info("Quiz import completed: {}", result.getMessage());
        return result;
    }

    private HierarchicalImportResult processRows(
            List<QuizExcelImportRow> rows,
            Type packageType,
            String userId,
            boolean isPrivate) {

        HierarchicalImportResult result = HierarchicalImportResult.builder()
                .packageIds(new ArrayList<>())
                .subjectIds(new ArrayList<>())
                .slotIds(new ArrayList<>())
                .folderIds(new ArrayList<>())
                .studySetIds(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();

        // Current hierarchy context
        Package currentPackage = null;
        Subject currentSubject = null;
        Slot currentSlot = null;
        Folder currentFolder = null;
        StudySet currentStudySet = null;

        // Current quiz building context
        String currentQuizTitle = null;
        String currentQuizDescription = null;
        DifficultyLevel currentQuizDifficulty = DifficultyLevel.MEDIUM;
        Integer currentTimeLimitSeconds = 0;
        Integer currentPassingScore = 70;

        // Current question building context
        List<CreateQuestionRequest> currentQuestions = new ArrayList<>();
        CreateQuestionRequest currentQuestion = null;
        QuestionType currentQuestionType = null;
        List<CreateQuestionRequest.OptionData> currentOptions = null;
        List<CreateQuestionRequest.BlankData> currentBlanks = null;
        List<CreateQuestionRequest.MatchingPairData> currentMatchingPairs = null;
        List<CreateQuestionRequest.ChunkData> currentChunks = null;

        for (QuizExcelImportRow row : rows) {
            if (row.isEmpty()) {
                continue;
            }

            if (!validationService.validateRowContext(row, currentStudySet, currentQuizTitle, currentQuestionType,
                    result)) {
                continue;
            }

            // === Hierarchy levels (same as AbstractHierarchicalImportService) ===

            // Package level
            if (row.hasPackageData()) {
                // Save pending quiz before moving to new hierarchy
                savePendingQuiz(currentStudySet, currentQuizTitle, currentQuizDescription,
                        currentQuizDifficulty, currentTimeLimitSeconds, currentPassingScore,
                        currentQuestions, currentQuestion, currentQuestionType,
                        currentOptions, currentBlanks, currentMatchingPairs, currentChunks,
                        userId, result);

                Package newPackage = Package.builder()
                        .name(row.getPackageName().trim())
                        .description(row.getPackageDescription() != null ? row.getPackageDescription().trim() : null)
                        .type(packageType)
                        .userId(userId)
                        .subjects(new ArrayList<>())
                        .folders(new ArrayList<>())
                        .build();
                currentPackage = packageRepository.save(newPackage);
                result.getPackageIds().add(currentPackage.getId());
                result.setTotalPackages(result.getTotalPackages() + 1);
                log.debug("Created package: {}", currentPackage.getName());

                currentSubject = null;
                currentSlot = null;
                currentFolder = null;
                currentStudySet = null;
                currentQuizTitle = null;
                currentQuestions = new ArrayList<>();
                currentQuestion = null;
                currentQuestionType = null;
            }

            // Subject level
            if (row.hasSubjectData()) {
                savePendingQuiz(currentStudySet, currentQuizTitle, currentQuizDescription,
                        currentQuizDifficulty, currentTimeLimitSeconds, currentPassingScore,
                        currentQuestions, currentQuestion, currentQuestionType,
                        currentOptions, currentBlanks, currentMatchingPairs, currentChunks,
                        userId, result);

                if (currentPackage == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_SUBJECT,
                            "Subject found without a package. Skipped.");
                    continue;
                }
                Subject newSubject = Subject.builder()
                        .name(row.getSubjectName().trim())
                        .code(row.getSubjectCode() != null ? row.getSubjectCode().trim() : "")
                        .description(row.getSubjectDescription() != null ? row.getSubjectDescription().trim() : null)
                        .userId(userId)
                        .slots(new ArrayList<>())
                        .folders(new ArrayList<>())
                        .build();
                newSubject.setPackageEntity(currentPackage);
                currentSubject = subjectRepository.save(newSubject);
                currentPackage.addSubject(currentSubject);
                result.getSubjectIds().add(currentSubject.getId());
                result.setTotalSubjects(result.getTotalSubjects() + 1);

                currentSlot = null;
                currentFolder = null;
                currentStudySet = null;
                currentQuizTitle = null;
                currentQuestions = new ArrayList<>();
                currentQuestion = null;
                currentQuestionType = null;
            }

            // Slot level
            if (row.hasSlotData()) {
                savePendingQuiz(currentStudySet, currentQuizTitle, currentQuizDescription,
                        currentQuizDifficulty, currentTimeLimitSeconds, currentPassingScore,
                        currentQuestions, currentQuestion, currentQuestionType,
                        currentOptions, currentBlanks, currentMatchingPairs, currentChunks,
                        userId, result);

                if (currentSubject == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_SLOT,
                            "Slot found without a subject. Skipped.");
                    continue;
                }
                Slot newSlot = Slot.builder()
                        .name(row.getSlotName().trim())
                        .slotNumber(row.getSlotNumber() != null ? row.getSlotNumber().trim() : null)
                        .description(row.getSlotDescription() != null ? row.getSlotDescription().trim() : null)
                        .userId(userId)
                        .folders(new ArrayList<>())
                        .build();
                newSlot.setSubject(currentSubject);
                currentSlot = slotRepository.save(newSlot);
                currentSubject.addSlot(currentSlot);
                result.getSlotIds().add(currentSlot.getId());
                result.setTotalSlots(result.getTotalSlots() + 1);

                currentFolder = null;
                currentStudySet = null;
                currentQuizTitle = null;
                currentQuestions = new ArrayList<>();
                currentQuestion = null;
                currentQuestionType = null;
            }

            // Folder level
            if (row.hasFolderData()) {
                savePendingQuiz(currentStudySet, currentQuizTitle, currentQuizDescription,
                        currentQuizDifficulty, currentTimeLimitSeconds, currentPassingScore,
                        currentQuestions, currentQuestion, currentQuestionType,
                        currentOptions, currentBlanks, currentMatchingPairs, currentChunks,
                        userId, result);

                if (currentPackage == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_FOLDER,
                            "Folder found without a package. Skipped.");
                    continue;
                }

                Folder newFolder = Folder.builder()
                        .name(row.getFolderName().trim())
                        .description(row.getFolderDescription() != null ? row.getFolderDescription().trim() : null)
                        .userId(userId)
                        .isPrivate(isPrivate)
                        .studySets(new ArrayList<>())
                        .build();

                if (currentSlot != null) {
                    newFolder.setSlot(currentSlot);
                } else if (currentSubject != null) {
                    newFolder.setSubject(currentSubject);
                } else {
                    newFolder.setPackageEntity(currentPackage);
                }

                currentFolder = folderRepository.save(newFolder);
                result.getFolderIds().add(currentFolder.getId());
                result.setTotalFolders(result.getTotalFolders() + 1);

                currentStudySet = null;
                currentQuizTitle = null;
                currentQuestions = new ArrayList<>();
                currentQuestion = null;
                currentQuestionType = null;
            }

            // StudySet level
            if (row.hasStudySetData()) {
                savePendingQuiz(currentStudySet, currentQuizTitle, currentQuizDescription,
                        currentQuizDifficulty, currentTimeLimitSeconds, currentPassingScore,
                        currentQuestions, currentQuestion, currentQuestionType,
                        currentOptions, currentBlanks, currentMatchingPairs, currentChunks,
                        userId, result);

                if (currentFolder == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_STUDY_SET,
                            "Study set found without a folder. Skipped.");
                    continue;
                }

                StudySet newStudySet = StudySet.builder()
                        .title(row.getStudySetName().trim())
                        .description(row.getStudySetDescription() != null ? row.getStudySetDescription().trim() : null)
                        .userId(userId)
                        .isPrivate(isPrivate)
                        .build();
                currentStudySet = studySetRepository.save(newStudySet);
                currentFolder.addStudySet(currentStudySet);
                folderRepository.save(currentFolder);
                result.getStudySetIds().add(currentStudySet.getId());
                result.setTotalStudySets(result.getTotalStudySets() + 1);

                currentQuizTitle = null;
                currentQuestions = new ArrayList<>();
                currentQuestion = null;
                currentQuestionType = null;
            }

            // === Quiz level ===
            if (row.hasQuizData()) {
                // Save previous quiz if exists
                savePendingQuiz(currentStudySet, currentQuizTitle, currentQuizDescription,
                        currentQuizDifficulty, currentTimeLimitSeconds, currentPassingScore,
                        currentQuestions, currentQuestion, currentQuestionType,
                        currentOptions, currentBlanks, currentMatchingPairs, currentChunks,
                        userId, result);

                currentQuizTitle = row.getQuizTitle().trim();
                currentQuizDescription = row.getQuizDescription() != null ? row.getQuizDescription().trim() : null;
                currentQuizDifficulty = questionImportHelper.parseDifficulty(row.getQuizDifficulty());
                currentTimeLimitSeconds = validationService.parseIntWithValidation(row.getTimeLimitSeconds(), 0, 0,
                        7200,
                        row.getRowNumber(), "TimeLimitSeconds", result);
                currentPassingScore = validationService.parseIntWithValidation(row.getPassingScore(), 70, 0, 100,
                        row.getRowNumber(), "PassingScore", result);
                currentQuestions = new ArrayList<>();
                currentQuestion = null;
                currentQuestionType = null;
            }

            // === Question level ===
            if (row.hasQuestionData()) {
                // Finalize previous question
                if (currentQuestion != null) {
                    questionImportHelper.finalizeQuestion(currentQuestion, currentQuestionType,
                            currentOptions, currentBlanks, currentMatchingPairs, currentChunks);
                    currentQuestions.add(currentQuestion);
                }

                currentQuestionType = questionImportHelper.parseQuestionType(row.getQuestionType());
                if (currentQuestionType == null) {
                    result.addWarning(row.getRowNumber(), ImportWarning.WarningType.MISSING_DEFINITION,
                            "Invalid question type: " + row.getQuestionType() + ". Skipped.");
                    currentQuestion = null;
                    continue;
                }

                currentQuestion = CreateQuestionRequest.builder()
                        .questionType(currentQuestionType)
                        .questionText(row.getQuestionText() != null ? row.getQuestionText().trim() : null)
                        .explanation(row.getExplanation() != null ? row.getExplanation().trim() : null)
                        .points(validationService.parseIntWithValidation(row.getPoints(), 1, 1, 100,
                                row.getRowNumber(), "Points", result))
                        .difficulty(questionImportHelper.parseDifficulty(row.getQuestionDifficulty()))
                        .build();

                if (!validationService.validateQuestionDefinition(currentQuestion, row.getRowNumber(), result)) {
                    currentQuestion = null;
                    currentQuestionType = null;
                    continue;
                }

                // Initialize sub-data lists
                currentOptions = new ArrayList<>();
                currentBlanks = new ArrayList<>();
                currentMatchingPairs = new ArrayList<>();
                currentChunks = new ArrayList<>();

                // Set type-specific fields
                questionImportHelper.setTypeSpecificFields(currentQuestion, currentQuestionType, row);

                // Parse first sub-data row (same row as question)
                questionImportHelper.parseSubData(currentQuestionType, row,
                        currentOptions, currentBlanks, currentMatchingPairs, currentChunks);
            }

            // === Sub-data only (additional options/blanks/pairs/chunks) ===
            if (row.hasSubDataOnly() && currentQuestionType != null) {
                questionImportHelper.parseSubData(currentQuestionType, row,
                        currentOptions, currentBlanks, currentMatchingPairs, currentChunks);
            }
        }

        // Save final pending quiz
        savePendingQuiz(currentStudySet, currentQuizTitle, currentQuizDescription,
                currentQuizDifficulty, currentTimeLimitSeconds, currentPassingScore,
                currentQuestions, currentQuestion, currentQuestionType,
                currentOptions, currentBlanks, currentMatchingPairs, currentChunks,
                userId, result);

        return result;
    }

    /**
     * Save pending quiz if there's quiz data accumulated
     */
    private void savePendingQuiz(
            StudySet currentStudySet,
            String quizTitle,
            String quizDescription,
            DifficultyLevel quizDifficulty,
            Integer timeLimitSeconds,
            Integer passingScore,
            List<CreateQuestionRequest> questions,
            CreateQuestionRequest pendingQuestion,
            QuestionType pendingQuestionType,
            List<CreateQuestionRequest.OptionData> pendingOptions,
            List<CreateQuestionRequest.BlankData> pendingBlanks,
            List<CreateQuestionRequest.MatchingPairData> pendingPairs,
            List<CreateQuestionRequest.ChunkData> pendingChunks,
            String userId,
            HierarchicalImportResult result) {

        if (quizTitle == null || currentStudySet == null) {
            return;
        }

        // Finalize pending question
        if (pendingQuestion != null) {
            questionImportHelper.finalizeQuestion(pendingQuestion, pendingQuestionType,
                    pendingOptions, pendingBlanks, pendingPairs, pendingChunks);
            questions.add(pendingQuestion);
        }

        List<CreateQuestionRequest> validQuestions = validationService.filterValidQuestions(questions, quizTitle,
                result);

        if (validQuestions.isEmpty()) {
            result.addWarning(0, ImportWarning.WarningType.MISSING_DEFINITION,
                    "Quiz '" + quizTitle + "' has no valid questions. Quiz skipped.");
            return;
        }

        // Build CreateQuizRequest
        CreateQuizRequest request = CreateQuizRequest.builder()
                .studySetId(currentStudySet.getId())
                .title(quizTitle)
                .description(quizDescription)
                .difficulty(quizDifficulty)
                .timeLimitSeconds(timeLimitSeconds)
                .passingScore(passingScore)
                .shuffleQuestions(true)
                .questions(validQuestions)
                .build();

        try {
            quizService.createQuiz(request, userId);
            result.setTotalContentItems(result.getTotalContentItems() + 1);
            log.debug("Created quiz: {} with {} questions", quizTitle, validQuestions.size());
        } catch (Exception e) {
            log.error("Failed to create quiz '{}': {}", quizTitle, e.getMessage());
            result.addWarning(0, ImportWarning.WarningType.MISSING_DEFINITION,
                    "Failed to create quiz '" + quizTitle + "': " + e.getMessage());
        }
    }

}
