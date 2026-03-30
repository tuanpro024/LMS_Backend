package com.lms.quiz.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single row in the Quiz Excel import file.
 * Hierarchy: Package -&gt; Folder -&gt; StudySet -&gt; Quiz -&gt; Questions
 *
 * Columns:
 * PackageName | PackageDescription | FolderName | FolderDescription |
 * StudySetName | StudySetDescription | QuizTitle | QuizDescription |
 * QuizDifficulty | TimeLimitSeconds | PassingScore |
 * QuestionType | QuestionText | Explanation | Points | QuestionDifficulty |
 * Data1 | Data2 | Data3 | Data4 | Data5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizExcelImportRow {

    private int rowNumber;

    // Package level
    private String packageName;
    private String packageDescription;

    // Folder level
    private String folderName;
    private String folderDescription;

    // StudySet level
    private String studySetName;
    private String studySetDescription;

    // Quiz level
    private String quizTitle;
    private String quizDescription;
    private String quizDifficulty; // EASY / MEDIUM / HARD
    private String timeLimitSeconds; // integer as string
    private String passingScore; // integer as string

    // Question level
    private String questionType; // MULTIPLE_CHOICE / FILL_IN_BLANK / MATCHING_PAIRS / SENTENCE_BUILDER
    private String questionText;
    private String explanation;
    private String points; // integer as string
    private String questionDifficulty; // EASY / MEDIUM / HARD

    // Type-specific data
    private String data1;
    private String data2;
    private String data3;
    private String data4;
    private String data5;

    // ========== Helper methods ==========

    public boolean hasPackageData() {
        return packageName != null && !packageName.trim().isEmpty();
    }

    public boolean hasFolderData() {
        return folderName != null && !folderName.trim().isEmpty();
    }

    public boolean hasStudySetData() {
        return studySetName != null && !studySetName.trim().isEmpty();
    }

    public boolean hasQuizData() {
        return quizTitle != null && !quizTitle.trim().isEmpty();
    }

    public boolean hasQuestionData() {
        return questionType != null && !questionType.trim().isEmpty();
    }

    public boolean hasSubDataOnly() {
        return !hasQuestionData() && hasAnySubData();
    }

    private boolean hasAnySubData() {
        return isNotBlank(data1) || isNotBlank(data2) || isNotBlank(data3)
                || isNotBlank(data4) || isNotBlank(data5);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public boolean isEmpty() {
        return !hasPackageData() && !hasFolderData() && !hasStudySetData()
                && !hasQuizData() && !hasQuestionData() && !hasSubDataOnly();
    }
}
