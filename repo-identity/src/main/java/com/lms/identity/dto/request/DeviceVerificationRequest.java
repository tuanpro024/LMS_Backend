package com.lms.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceVerificationRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "OTP is required")
    private String otp;
}