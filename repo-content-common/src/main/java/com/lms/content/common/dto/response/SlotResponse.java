package com.lms.content.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {

    private String id;
    private String name;
    private String slotNumber;
    private String description;
    private String userId;
    private String subjectId;
    private Instant createdAt;
    private Instant updatedAt;
}
