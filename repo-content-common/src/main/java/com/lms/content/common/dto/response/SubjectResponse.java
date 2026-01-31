package com.lms.content.common.dto.response;

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
public class SubjectResponse {

    private String id;
    private String name;
    private String code;
    private String description;
    private String userId;
    private String packageId;
    private List<SlotResponse> slots;
    private Instant createdAt;
    private Instant updatedAt;
}
