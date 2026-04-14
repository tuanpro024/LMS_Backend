package com.lms.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPurchasedEvent {
    private String userId;
    private String orderId;
    private int durationInDays;
    private String packageName;
}
