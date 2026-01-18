package com.lms.writing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetResponse {
    private String id;
    private String title;
    private String description;
    private boolean isPrivate;
    private String userId;
    private List<WordResponse> words;
    private Instant createdAt;
    private Instant updatedAt;
}
