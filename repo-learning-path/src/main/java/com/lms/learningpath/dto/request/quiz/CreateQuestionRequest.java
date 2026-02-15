package com.lms.learningpath.dto.request.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a quiz question.
 * Copied from repo-quiz for import functionality.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequest {

    private QuestionType questionType;
    private String questionText;
    private String questionMediaUrl;
    private String explanation;
    private Integer points;
    private DifficultyLevel difficulty;

    // FILL_IN_BLANK specific
    private FillBlankMode fillBlankMode;
    private String sentenceTemplate;

    // SENTENCE_BUILDER specific
    private String correctSentence;
    private String translationHint;

    // Sub-data for different question types
    private List<OptionData> options;
    private List<BlankData> blanks;
    private List<MatchingPairData> matchingPairs;
    private List<ChunkData> sentenceChunks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionData {
        private String content;
        private String mediaUrl;
        private Boolean isCorrect;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlankData {
        private Integer blankIndex;
        private String correctAnswer;
        private String acceptedAnswers;
        private String hint;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchingPairData {
        private String prompt;
        private String answer;
        private String promptMediaUrl;
        private String answerMediaUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkData {
        private String content;
        private Integer correctPosition;
        private Boolean isDistractor;
    }
}
