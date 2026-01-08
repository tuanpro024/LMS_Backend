package com.lms.flashcard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderResponse {

    private String id;
    private String name;
    private String description;
    private String color;
    private boolean isPrivate;
    private String userId;
    private String parentFolderId;
    private List<String> studySetIds;
    private Instant createdAt;
    private Instant updatedAt;
}
