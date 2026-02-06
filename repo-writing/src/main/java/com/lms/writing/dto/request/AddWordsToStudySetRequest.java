package com.lms.writing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddWordsToStudySetRequest {

    @NotBlank(message = "StudySet ID is required")
    private String studySetId;

    @NotEmpty(message = "Words list cannot be empty")
    @Valid
    private List<CreateWordRequest> words;
}
