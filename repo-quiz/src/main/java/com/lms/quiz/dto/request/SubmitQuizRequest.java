package com.lms.quiz.dto.request;

import lombok.*;

import java.util.List;

/**
 * User submit toàn bộ bài quiz.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizRequest {

    private String quizId;
    private List<SubmitAnswerRequest> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitAnswerRequest {
        private String questionId;

        // MULTIPLE_CHOICE: ID đáp án được chọn
        private String selectedOptionId;

        // FILL_IN_BLANK: Danh sách đáp án cho từng blank
        private List<BlankAnswer> blankAnswers;

        // MATCHING_PAIRS: Danh sách cặp user đã nối
        private List<MatchAnswer> matchAnswers;

        // SENTENCE_BUILDER: Danh sách ID chunk theo thứ tự user sắp xếp
        private List<String> orderedChunkIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlankAnswer {
        private Integer blankIndex;
        private String answer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchAnswer {
        private String promptId;
        private String answerId;
    }
}
