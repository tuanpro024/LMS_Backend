package com.lms.onllearning.dto.response;

import java.time.LocalDateTime;

public record LeadRegistrationResponse(
    String id,
    String syllabusId,
    String syllabusName,
    String fullName,
    String email,
    String phone,
    String note,
    LocalDateTime registeredAt
) {}
