package com.lms.kanjiorigin.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Extra data from columns beyond HierarchicalImportRow (AA onwards)
 * for kanji-origin specific fields + question data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanjiExtraRowData {

    // Lesson data (AA-AB)
    private String lessonTitle;        // AA (col 26)
    private String lessonDescription;  // AB (col 27)

    // KanjiOrigin extra fields (AC-AG)
    private String originTextCn;       // AC (col 28)
    private String originTextEn;       // AD (col 29)
    private String strokeAnimationUrl; // AE (col 30)
    private String exampleMeaningVi;   // AF (col 31)
    private String exampleMeaningEn;   // AG (col 32)

    // Question data (AH-AJ)
    private String questionContent;    // AH (col 33)
    private String correctAnswer;      // AI (col 34)
    private String wrongOptionsRaw;    // AJ (col 35) - pipe-separated

    public boolean hasQuestionData() {
        return questionContent != null && !questionContent.trim().isEmpty();
    }

    public List<String> getWrongOptionsList() {
        if (wrongOptionsRaw == null || wrongOptionsRaw.trim().isEmpty()) {
            return List.of();
        }
        return java.util.Arrays.stream(wrongOptionsRaw.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
