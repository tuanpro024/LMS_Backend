package com.lms.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipExpiryEvent {
    private String userId;
    private String email;
    private String fullName;
    private Instant expiryDate;
    private int daysRemaining;
}
