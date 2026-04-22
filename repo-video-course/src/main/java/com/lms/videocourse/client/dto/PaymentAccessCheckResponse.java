package com.lms.videocourse.client.dto;

import lombok.Data;

/**
 * Response DTO from repo-payment for access check.
 */
@Data
public class PaymentAccessCheckResponse {
    private boolean hasAccess;
    private String accessType;
}
