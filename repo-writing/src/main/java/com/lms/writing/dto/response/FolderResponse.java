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
public class FolderResponse {
    private String id;
    private String name;
    private String description;
    private String color;
    private boolean isPrivate;
    private String userId;
    private String packageId;
    private String subjectId;
    private String slotId;
    private List<StudySetResponse> studySets;
    private Instant createdAt;
    private Instant updatedAt;
}
