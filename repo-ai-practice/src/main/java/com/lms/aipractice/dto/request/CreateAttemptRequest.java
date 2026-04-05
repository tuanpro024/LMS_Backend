package com.lms.aipractice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAttemptRequest {

    @NotBlank
    private String studySetId;
}
