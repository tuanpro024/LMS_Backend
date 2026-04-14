package com.lms.notification.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.common.dto.ApiResponse;
import com.lms.common.event.MembershipExpiryEvent;
import com.lms.common.event.MembershipPurchasedEvent;
import com.lms.notification.client.IdentityClient;
import com.lms.notification.client.dto.UserProfileDto;
import com.lms.notification.service.Impl.MembershipEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipEventListener {

    private final MembershipEmailService emailService;
    private final IdentityClient identityClient;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "membership.purchased", groupId = "notification-group")
    public void handleMembershipPurchased(String message) {
        log.info("Processing raw membership.purchased event: {}", message);
        
        try {
            MembershipPurchasedEvent event = objectMapper.readValue(message, MembershipPurchasedEvent.class);
            ApiResponse<UserProfileDto> response = identityClient.getProfile(event.getUserId());
            if (response != null && response.data() != null) {
                UserProfileDto profile = response.data();
                Instant expiryDate = Instant.now().plus(event.getDurationInDays(), ChronoUnit.DAYS);
                
                emailService.sendPurchaseSuccessEmail(profile.getEmail(), profile.getFullName(), expiryDate);
            }
        } catch (Exception e) {
            log.error("Failed to process membership purchase event message", e);
        }
    }

    @KafkaListener(topics = "membership.expiring", groupId = "notification-group")
    public void handleMembershipExpiring(String message) {
        log.info("Processing raw membership.expiring event: {}", message);
        try {
            MembershipExpiryEvent event = objectMapper.readValue(message, MembershipExpiryEvent.class);
            emailService.sendExpiryWarningEmail(event.getEmail(), event.getFullName(), event.getExpiryDate(), event.getDaysRemaining());
        } catch (Exception e) {
            log.error("Failed to process membership expiring event message", e);
        }
    }
}
