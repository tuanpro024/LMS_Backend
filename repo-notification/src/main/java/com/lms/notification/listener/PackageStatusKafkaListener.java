package com.lms.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.common.event.PackageStatusEvent;
import com.lms.notification.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PackageStatusKafkaListener {

    private final CampaignService campaignService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${package.status.kafka.topic:package.status.events}", groupId = "${package.status.kafka.group:notification-package-status}")
    public void onMessage(String message) {
        try {
            PackageStatusEvent event = objectMapper.readValue(message, PackageStatusEvent.class);
            if (!"DRAFT".equalsIgnoreCase(event.status())) {
                return;
            }

            campaignService.sendPackageDraftNotificationToAllUsers(event);
        } catch (Exception ex) {
            log.warn("Failed to process package status event message: {}", ex.getMessage());
        }
    }
}
