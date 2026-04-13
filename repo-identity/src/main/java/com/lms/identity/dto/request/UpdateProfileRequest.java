package com.lms.identity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lms.identity.entity.Gender;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    @NotBlank
    @Size(max = 120)
    private String fullName;

    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 255)
    private String address;

    @NotNull
    private Gender gender;

    @NotNull
    @Past
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
}
