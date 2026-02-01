package com.lms.kanjiorigin.dto.request;

import jakarta.validation.Valid;
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
public class AssignQuestionToMultipleLessonsRequest {

    @NotBlank(message = "Question ID is required")
    private String kanjiQuestionId;

    @NotEmpty(message = "At least one lesson assignment is required")
    @Valid
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
