package com.lms.multimedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleUploadResponse {

    private String subtitleId;
    private String message;
    private SubtitleResponse subtitle;

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
