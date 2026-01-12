package com.lms.flashcard.dto.response;

import com.lms.flashcard.entity.PackageType;
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
public class PackageResponse {

    private String id;
    private String name;
    private String subjectCode;
    private String slot;
    private PackageType type;
    private String description;
    private String userId;
    private List<FolderResponse> folders;
    private Instant createdAt;
    private Instant updatedAt;
}
