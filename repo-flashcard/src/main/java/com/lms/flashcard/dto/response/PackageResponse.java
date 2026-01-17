package com.lms.flashcard.dto.response;

import com.lms.flashcard.entity.TypeName;
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
    private TypeName type;
    private String description;
    private String userId;
    private List<SubjectResponse> subjects;
    private List<FolderResponse> folders;
    private Instant createdAt;
    private Instant updatedAt;
}
