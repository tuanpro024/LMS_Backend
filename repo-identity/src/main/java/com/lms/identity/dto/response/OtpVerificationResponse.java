package com.lms.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OtpVerificationResponse {
    private final String message;
    private final String email;
    private final Long expiresInSeconds;
}
