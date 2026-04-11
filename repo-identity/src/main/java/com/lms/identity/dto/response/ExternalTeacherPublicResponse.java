package com.lms.identity.dto.response;

public record ExternalTeacherPublicResponse(
        String id,
        String fullName,
        String avatarUrl,
        String fullDescription,
        String qualification,
        Double rating,
        String shortDescription,
        String teachingStyle,
        String videoIntroLink
) {
}
