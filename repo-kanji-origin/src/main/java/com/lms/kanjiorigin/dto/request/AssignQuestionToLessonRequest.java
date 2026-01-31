package com.lms.kanjiorigin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignQuestionToLessonRequest {

    @NotBlank(message = "Lesson ID is required")
    private String kanjiLessonId;

    @NotBlank(message = "Question ID is required")
    private String kanjiQuestionId;

    @NotNull(message = "Content index is required")
    private Integer contentIndex;
}
