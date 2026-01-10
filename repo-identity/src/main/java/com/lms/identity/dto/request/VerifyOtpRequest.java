package com.lms.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    @NotBlank(message = "Required parameter is missing value. (email)")
    @Email(message = "Input Parameter Error. Invalid data format. (email)")
    private String email;

    @NotBlank(message = "Required parameter is missing value. (otp)")
    @Pattern(regexp = "\\d{6}", message = "Input Parameter Error. OTP must be 6 digits. (otp)")
    private String otp;
}
