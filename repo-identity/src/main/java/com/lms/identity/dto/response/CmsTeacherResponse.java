package com.lms.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record CmsTeacherResponse(
    @JsonProperty("user_id") String userId,
    @JsonProperty("email") String email,
    @JsonProperty("full_name") String fullName,
    @JsonProperty("phone_number") String phoneNumber,
    @JsonProperty("address") String address,
    @JsonProperty("avatar_url") String avatarUrl,
    @JsonProperty("status") String status,
    @JsonProperty("qualification") String qualification,
    @JsonProperty("teaching_style") String teachingStyle,
    @JsonProperty("video_intro_link") String videoIntroLink,
    @JsonProperty("short_description") String shortDescription,
    @JsonProperty("full_description") String fullDescription,
    @JsonProperty("rating") Double rating,
    @JsonProperty("total_classes") Integer totalClasses,
    @JsonProperty("total_students") Integer totalStudents,
    @JsonProperty("total_sessions") Integer totalSessions
) {}
