package com.lms.identity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lms.identity.entity.Gender;
import com.lms.identity.entity.UserStatus;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class AdminUpdateUserRequest {
    private UserStatus status;

    private Set<String> roles;

    @Size(max = 120)
    private String fullName;

    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 255)
    private String address;

    private Gender gender;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
}
