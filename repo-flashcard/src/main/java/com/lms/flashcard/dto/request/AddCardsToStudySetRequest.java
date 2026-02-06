package com.lms.flashcard.dto.request;

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
public class AddCardsToStudySetRequest {

    @NotBlank(message = "StudySet ID is required")
    private String studySetId;

    @NotEmpty(message = "Cards list cannot be empty")
    @Valid
    private List<CreateCardRequest> cards;
}
