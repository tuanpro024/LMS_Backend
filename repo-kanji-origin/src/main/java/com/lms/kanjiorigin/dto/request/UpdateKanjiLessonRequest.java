package com.lms.kanjiorigin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKanjiLessonRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Min(value = 0, message = "Content index must be non-negative")
    private Integer contentIndex;
}
