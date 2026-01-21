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
public class SubjectResponse {
    private String id;
    private String name;
    private String code;
    private String description;
    private String userId;
    private String packageId;
    private List<SlotResponse> slots;
    private List<FolderResponse> folders;
    private Instant createdAt;
    private Instant updatedAt;
}
