package com.lms.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lms.identity.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class ProfileResponse {
    private final String id;
    private final String email;
    private final String fullName;
    private final String phoneNumber;
    private final String avatarUrl;
    private final String address;
    private final Set<String> roles;
    @JsonProperty("isPremium")
    private final boolean isPremium;
    @JsonProperty("premiumExpiryDate")
    private final java.time.Instant premiumExpiryDate;
}
