package com.lms.multimedia.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AddVideosToStudySetRequest {
    @NotEmpty(message = "Video codes cannot be empty")
    private List<String> videoCodes;
}
