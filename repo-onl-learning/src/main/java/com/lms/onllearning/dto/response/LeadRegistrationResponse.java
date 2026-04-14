package com.lms.onllearning.dto.response;

import java.time.LocalDateTime;

public record LeadRegistrationResponse(
        String id,
        String code,
        String name,
        String courseType,
        String fullName,
        String email,
        String phone,
        String note,
        LocalDateTime registeredAt) {
}
