package com.lms.quiz.service.validation;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.excel.ImportWarning;
import com.lms.content.common.entity.StudySet;
import com.lms.quiz.dto.excel.QuizExcelImportRow;
import com.lms.quiz.dto.request.CreateQuestionRequest;
import com.lms.quiz.entity.enums.QuestionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuizImportValidationService {

    public boolean validateRowContext(
            QuizExcelImportRow row,
            StudySet currentStudySet,
            String currentQuizTitle,
            QuestionType currentQuestionType,
            HierarchicalImportResult result) {

        // Allow same-row hierarchy + quiz (common in import templates).
        if (row.hasQuizData() && currentStudySet == null && !row.hasStudySetData()) {
            result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_CONTENT_ITEM,
                    "Quiz found without a study set context. Row skipped.");
            return false;
        }

        // Allow same-row quiz + first question.
        if (row.hasQuestionData() && currentQuizTitle == null && !row.hasQuizData()) {
            result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_CONTENT_ITEM,
                    "Question found without a quiz context. Row skipped.");
            return false;
        }

        if (row.hasSubDataOnly() && currentQuestionType == null) {
            result.addWarning(row.getRowNumber(), ImportWarning.WarningType.ORPHAN_CONTENT_ITEM,
                    "Question sub-data found without an active question. Row skipped.");
            return false;
        }

        return true;
    }

    public int parseIntWithValidation(
            String value,
            int defaultValue,
            int min,
            int max,
            int rowNumber,
            String fieldName,
            HierarchicalImportResult result) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < min || parsed > max) {
                result.addWarning(rowNumber, ImportWarning.WarningType.MISSING_DEFINITION,
                        fieldName + " must be in range [" + min + ", " + max + "]. Using default: "
                                + defaultValue);
                return defaultValue;
            }
            return parsed;
        } catch (NumberFormatException e) {
            result.addWarning(rowNumber, ImportWarning.WarningType.MISSING_DEFINITION,
                    fieldName + " must be an integer. Using default: " + defaultValue);
            return defaultValue;
        }
    }

    public boolean validateQuestionDefinition(
            CreateQuestionRequest question,
            int rowNumber,
            HierarchicalImportResult result) {
        if (question == null || question.getQuestionType() == null || isBlank(question.getQuestionText())) {
            result.addWarning(rowNumber, ImportWarning.WarningType.MISSING_DEFINITION,
                    "QuestionText is required. Question skipped.");
            return false;
        }
        return true;
    }

    public List<CreateQuestionRequest> filterValidQuestions(
            List<CreateQuestionRequest> questions,
            String quizTitle,
            HierarchicalImportResult result) {
        if (questions == null || questions.isEmpty()) {
            return new ArrayList<>();
        }
        return questions.stream()
                .filter(q -> validateQuestionStructure(q, quizTitle, result))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean validateQuestionStructure(
            CreateQuestionRequest question,
            String quizTitle,
            HierarchicalImportResult result) {
        if (question == null || question.getQuestionType() == null || isBlank(question.getQuestionText())) {
            return false;
        }

        switch (question.getQuestionType()) {
            case MULTIPLE_CHOICE:
                if (question.getOptions() == null || question.getOptions().size() < 2) {
                    result.addWarning(0, ImportWarning.WarningType.MISSING_DEFINITION,
                            "Quiz '" + quizTitle + "': MULTIPLE_CHOICE must have at least 2 options.");
                    return false;
                }
                boolean hasCorrectOption = question.getOptions().stream()
                        .anyMatch(o -> o != null && Boolean.TRUE.equals(o.getIsCorrect()));
                if (!hasCorrectOption) {
                    result.addWarning(0, ImportWarning.WarningType.MISSING_DEFINITION,
                            "Quiz '" + quizTitle + "': MULTIPLE_CHOICE must have at least 1 correct option.");
                    return false;
                }
                return true;

            case FILL_IN_BLANK:
                if (isBlank(question.getSentenceTemplate())) {
                    result.addWarning(0, ImportWarning.WarningType.MISSING_DEFINITION,
                            "Quiz '" + quizTitle + "': FILL_IN_BLANK requires sentenceTemplate.");
                    return false;
                }
                if (question.getBlanks() == null || question.getBlanks().isEmpty()) {
                    result.addWarning(0, ImportWarning.WarningType.MISSING_DEFINITION,
                            "Quiz '" + quizTitle + "': FILL_IN_BLANK requires at least 1 blank answer.");
                    return false;
                }
                return true;

            case MATCHING_PAIRS:
                if (question.getMatchingPairs() == null || question.getMatchingPairs().isEmpty()) {
                    result.addWarning(0, ImportWarning.WarningType.MISSING_DEFINITION,
                            "Quiz '" + quizTitle + "': MATCHING_PAIRS requires at least 1 pair.");
                    return false;
                }
                return true;

            case SENTENCE_BUILDER:
                if (isBlank(question.getCorrectSentence())) {
                    result.addWarning(0, ImportWarning.WarningType.MISSING_DEFINITION,
                            "Quiz '" + quizTitle + "': SENTENCE_BUILDER requires correctSentence.");
                    return false;
                }
                if (question.getSentenceChunks() == null || question.getSentenceChunks().isEmpty()) {
                    result.addWarning(0, ImportWarning.WarningType.MISSING_DEFINITION,
                            "Quiz '" + quizTitle + "': SENTENCE_BUILDER requires at least 1 chunk.");
                    return false;
                }
                return true;

            default:
                return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
