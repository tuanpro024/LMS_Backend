package com.lms.quiz.service.validation;

import com.lms.quiz.dto.excel.QuizExcelImportRow;
import com.lms.quiz.dto.request.CreateQuestionRequest;
import com.lms.quiz.entity.enums.DifficultyLevel;
import com.lms.quiz.entity.enums.FillBlankMode;
import com.lms.quiz.entity.enums.QuestionType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuizQuestionImportHelper {

    public DifficultyLevel parseDifficulty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return DifficultyLevel.MEDIUM;
        }
        try {
            return DifficultyLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return DifficultyLevel.MEDIUM;
        }
    }

    public QuestionType parseQuestionType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return QuestionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void finalizeQuestion(
            CreateQuestionRequest question,
            QuestionType questionType,
            List<CreateQuestionRequest.OptionData> options,
            List<CreateQuestionRequest.BlankData> blanks,
            List<CreateQuestionRequest.MatchingPairData> matchingPairs,
            List<CreateQuestionRequest.ChunkData> chunks) {

        if (question == null || questionType == null) {
            return;
        }

        switch (questionType) {
            case MULTIPLE_CHOICE:
                question.setOptions(options);
                break;
            case FILL_IN_BLANK:
                question.setBlanks(blanks);
                if (options != null && !options.isEmpty()) {
                    question.setOptions(options);
                }
                break;
            case MATCHING_PAIRS:
                question.setMatchingPairs(matchingPairs);
                break;
            case SENTENCE_BUILDER:
                question.setSentenceChunks(chunks);
                break;
            default:
                break;
        }
    }

    public void setTypeSpecificFields(
            CreateQuestionRequest question,
            QuestionType questionType,
            QuizExcelImportRow row) {

        switch (questionType) {
            case FILL_IN_BLANK:
                question.setSentenceTemplate(row.getData1() != null ? row.getData1().trim() : null);
                question.setFillBlankMode(parseFillBlankMode(row.getData2()));
                break;
            case SENTENCE_BUILDER:
                question.setCorrectSentence(row.getData1() != null ? row.getData1().trim() : null);
                question.setTranslationHint(row.getData2() != null ? row.getData2().trim() : null);
                break;
            default:
                break;
        }
    }

    public void parseSubData(
            QuestionType questionType,
            QuizExcelImportRow row,
            List<CreateQuestionRequest.OptionData> options,
            List<CreateQuestionRequest.BlankData> blanks,
            List<CreateQuestionRequest.MatchingPairData> matchingPairs,
            List<CreateQuestionRequest.ChunkData> chunks) {

        if (questionType == null || (row.getData1() == null && row.getData3() == null)) {
            return;
        }

        switch (questionType) {
            case MULTIPLE_CHOICE:
                if (row.getData1() != null) {
                    options.add(CreateQuestionRequest.OptionData.builder()
                            .content(row.getData1().trim())
                            .mediaUrl(null)
                            .isCorrect(parseBoolean(row.getData2()))
                            .build());
                }
                break;

            case FILL_IN_BLANK:
                if (row.hasSubDataOnly() || (!row.hasQuestionData() && row.getData1() != null)) {
                    blanks.add(CreateQuestionRequest.BlankData.builder()
                            .blankIndex(blanks.size())
                            .correctAnswer(row.getData1() != null ? row.getData1().trim() : null)
                            .acceptedAnswers(row.getData2() != null ? row.getData2().trim() : null)
                            .hint(row.getData3() != null ? row.getData3().trim() : null)
                            .build());
                }
                break;

            case MATCHING_PAIRS:
                if (row.getData1() != null && row.getData2() != null) {
                    matchingPairs.add(CreateQuestionRequest.MatchingPairData.builder()
                            .prompt(row.getData1().trim())
                            .answer(row.getData2().trim())
                            .promptMediaUrl(null)
                            .answerMediaUrl(null)
                            .build());
                }
                break;

            case SENTENCE_BUILDER:
                String chunkContent = row.getData3();
                if (chunkContent != null && !chunkContent.trim().isEmpty()) {
                    chunks.add(CreateQuestionRequest.ChunkData.builder()
                            .content(chunkContent.trim())
                            .correctPosition(parseIntOrDefault(row.getData4(), chunks.size()))
                            .isDistractor(parseBoolean(row.getData5()))
                            .build());
                }
                break;
            default:
                break;
        }
    }

    private FillBlankMode parseFillBlankMode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return FillBlankMode.TYPE;
        }
        try {
            return FillBlankMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return FillBlankMode.TYPE;
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String normalized = value.trim().toUpperCase();
        return "TRUE".equals(normalized) || "1".equals(normalized) || "YES".equals(normalized);
    }
}
