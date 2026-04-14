package com.lms.identity.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.common.event.MembershipPurchasedEvent;
import com.lms.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdentityEventListener {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "membership.purchased", groupId = "identity-group")
    public void handleMembershipPurchased(String message) {
        log.info("Received raw MembershipPurchasedEvent: {}", message);
        try {
            MembershipPurchasedEvent event = objectMapper.readValue(message, MembershipPurchasedEvent.class);
            userService.updatePremiumStatus(event.getUserId(), true, event.getDurationInDays());
            log.info("Updated premium status for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to process MembershipPurchasedEvent message", e);
        }
    }
}
