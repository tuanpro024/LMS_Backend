package com.lms.quiz.dto.response;

import com.lms.quiz.entity.enums.DifficultyLevel;
import com.lms.quiz.entity.enums.FillBlankMode;
import com.lms.quiz.entity.enums.QuestionType;
import lombok.*;

import java.util.List;

/**
 * Response trả về cho FE - KHÔNG chứa đáp án đúng khi user đang làm bài.
 * Chỉ trả đáp án đúng trong QuizResultResponse sau khi submit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private String id;
    private Integer questionIndex;
    private QuestionType questionType;
    private String questionText;
    private String questionMediaUrl;
    private Integer points;
    private DifficultyLevel difficulty;
    private String explanation;

    // FILL_IN_BLANK
    private FillBlankMode fillBlankMode;
    private String sentenceTemplate;
    private List<BlankInfo> blanks;

    // MULTIPLE_CHOICE & word bank
    private List<OptionInfo> options;

    // MATCHING_PAIRS
    private List<PairInfo> pairInfos;

    // SENTENCE_BUILDER
    private String translationHint;
    private String correctSentence;
    private List<ChunkInfo> chunks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfo {
        private String id;
        private Integer optionIndex;
        private String content;
        private String mediaUrl;
        private Boolean isCorrect; // Chỉ populate cho admin view
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlankInfo {
        private Integer blankIndex;
        private String hint;
        private String correctAnswer;    // Chỉ populate cho admin view
        private String acceptedAnswers;  // JSON array, e.g. ["went","had gone"]
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PairInfo {
        private String id;
        private String prompt;
        private String promptMediaUrl;
        private String answer;
        private String answerMediaUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkInfo {
        private String id;
        private String content;
        private Boolean isDistractor;
        private Integer correctPosition; // Chỉ populate cho admin view
    }
}
