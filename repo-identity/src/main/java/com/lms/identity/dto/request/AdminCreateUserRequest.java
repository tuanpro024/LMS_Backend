package com.lms.identity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lms.identity.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.lms.identity.entity.UserStatus;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class AdminCreateUserRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
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

    private UserStatus status;

    private Set<String> roles;
}
