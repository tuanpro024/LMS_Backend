package com.lms.quiz.dto.request;

import com.lms.quiz.entity.enums.DifficultyLevel;
import com.lms.quiz.entity.enums.FillBlankMode;
import com.lms.quiz.entity.enums.QuestionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequest {

    @NotNull(message = "Question type is required")
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

    // Sub-data cho từng dạng
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
