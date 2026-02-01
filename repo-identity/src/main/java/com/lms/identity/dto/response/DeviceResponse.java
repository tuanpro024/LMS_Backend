package com.lms.identity.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeviceResponse {
    private String id;
    private String deviceId;
    private String name;
    private String type;
    private String os;
    private String browser;
    private String location;
    private LocalDateTime date;
    private boolean isCurrent;
}