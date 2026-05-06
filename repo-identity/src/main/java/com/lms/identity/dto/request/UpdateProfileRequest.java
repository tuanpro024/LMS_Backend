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

    // ===== Optional teacher profile enrichment fields =====

    @Size(max = 500, message = "Mô tả ngắn không được vượt quá 500 ký tự")
    private String shortDescription;

    @Size(max = 5000, message = "Mô tả chi tiết không được vượt quá 5000 ký tự")
    private String fullDescription;

    @Size(max = 300, message = "Phong cách giảng dạy không được vượt quá 300 ký tự")
    private String teachingStyle;

    @Size(max = 500, message = "Bằng cấp không được vượt quá 500 ký tự")
    private String qualification;

    @Size(max = 500, message = "Link video giới thiệu không được vượt quá 500 ký tự")
    private String videoIntroLink;
}
