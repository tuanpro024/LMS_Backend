package com.lms.kanjiorigin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequest {

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    @NotEmpty(message = "At least one wrong option is required")
    private List<String> wrongOptions;

    private List<LessonAssignment> lessonAssignments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonAssignment {
        @NotBlank(message = "Lesson ID is required")
        private String kanjiLessonId;

        @NotNull(message = "Content index is required")
        private Integer contentIndex;
    }
}

