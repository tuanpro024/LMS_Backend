package com.lms.quiz.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single row in the Quiz Excel import file.
 * Hierarchy: Package → Subject → Slot → Folder → StudySet → Quiz → Questions
 *
 * Columns:
 * A-B: Package (name, desc)
 * C-E: Subject (name, code, desc)
 * F-H: Slot (name, number, desc)
 * I-J: Folder (name, desc)
 * K: StudySet (name)
 * L-P: Quiz metadata (title, desc, difficulty, timeLimit, passingScore)
 * Q-U: Question common (type, text, explanation, points, difficulty)
 * V-Z: Question type-specific data (Data1-Data5)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizExcelImportRow {

    private int rowNumber;

    // Package level (A-B)
    private String packageName;
    private String packageDescription;

    // Subject level (C-E)
    private String subjectName;
    private String subjectCode;
    private String subjectDescription;

    // Slot level (F-H)
    private String slotName;
    private String slotNumber;
    private String slotDescription;

    // Folder level (I-J)
    private String folderName;
    private String folderDescription;

    // StudySet level (K)
    private String studySetName;

    // Quiz level (L-P)
    private String quizTitle;
    private String quizDescription;
    private String quizDifficulty; // EASY / MEDIUM / HARD
    private String timeLimitSeconds; // integer as string
    private String passingScore; // integer as string

    // Question level (Q-U)
    private String questionType; // MULTIPLE_CHOICE / FILL_IN_BLANK / MATCHING_PAIRS / SENTENCE_BUILDER
    private String questionText;
    private String explanation;
    private String points; // integer as string
    private String questionDifficulty; // EASY / MEDIUM / HARD

    // Type-specific data (V-Z)
    private String data1;
    private String data2;
    private String data3;
    private String data4;
    private String data5;

    // ========== Helper methods ==========

    public boolean hasPackageData() {
        return packageName != null && !packageName.trim().isEmpty();
    }

    public boolean hasSubjectData() {
        return subjectName != null && !subjectName.trim().isEmpty();
    }

    public boolean hasSlotData() {
        return slotName != null && !slotName.trim().isEmpty();
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

    /**
     * Row has sub-data only (option/blank/pair/chunk for previous question)
     */
    public boolean hasSubDataOnly() {
        return !hasQuestionData() && data1 != null && !data1.trim().isEmpty();
    }

    public boolean isEmpty() {
        return !hasPackageData() && !hasSubjectData() && !hasSlotData()
                && !hasFolderData() && !hasStudySetData() && !hasQuizData()
                && !hasQuestionData() && !hasSubDataOnly();
    }
}
