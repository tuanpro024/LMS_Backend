package com.lms.writing.dto.response;

import com.lms.writing.entity.TypeName;
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
