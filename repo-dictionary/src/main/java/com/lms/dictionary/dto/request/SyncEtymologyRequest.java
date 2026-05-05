package com.lms.dictionary.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncEtymologyRequest {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;
}
