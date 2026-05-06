package com.lms.identity.dto.response;

import com.lms.identity.entity.Gender;
import com.lms.identity.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Builder
public class AdminUserResponse {
    private final String id;
    private final String email;
    private final String fullName;
    private final String phoneNumber;
    private final String avatarUrl;
    private final String address;
    private final Gender gender;
    private final LocalDate dob;
    private final Set<String> roles;
    private final UserStatus status;

    // Teacher profile enrichment fields
    private final String shortDescription;
    private final String fullDescription;
    private final String teachingStyle;
    private final String qualification;
    private final String videoIntroLink;
}
