package com.lms.identity.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LoginHistoryResponse {
    private String id;
    private String deviceName;
    private String os;
    private String browser;
    private String ipAddress;
    private LocalDateTime loginTime;
}