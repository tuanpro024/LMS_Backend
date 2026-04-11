package com.lms.notification.service;

import com.lms.common.notification.NotificationEvent;
import com.lms.common.notification.NotificationPublisher;
import com.lms.common.notification.ResourceType;
import com.lms.notification.entity.ManualNotificationCampaign;
import com.lms.notification.entity.ManualNotificationRecipient;
import com.lms.notification.entity.enums.CampaignStatus;
import com.lms.notification.entity.enums.RecipientStatus;
import com.lms.notification.repository.ManualNotificationCampaignRepository;
import com.lms.notification.repository.ManualNotificationRecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignProcessorService {

    private final ManualNotificationCampaignRepository campaignRepository;
    private final ManualNotificationRecipientRepository recipientRepository;
    private final NotificationPublisher notificationPublisher;

    @Async
    public void processSpecificUsersCampaign(String campaignId) {
        log.info("Starting processing campaign async: {}", campaignId);
        ManualNotificationCampaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null) {
            log.warn("Campaign {} not found to process", campaignId);
            return;
        }

        try {
            int page = 0;
            int size = 500;
            Page<ManualNotificationRecipient> recipientPage;
            boolean hasFailures = false;

            do {
                recipientPage = recipientRepository.findByCampaignIdAndStatus(campaignId, RecipientStatus.PENDING, PageRequest.of(page, size));
                
                for (ManualNotificationRecipient recipient : recipientPage.getContent()) {
                    try {
                        NotificationEvent event = new NotificationEvent(
                                recipient.getUserId(),
                                "MANUAL_CAMPAIGN",
                                campaign.getTitle(),
                                campaign.getMessage(),
                                ResourceType.OTHER,
                                campaignId,
                                new HashMap<>(),
                                campaignId + "-" + recipient.getUserId()
                        );
                        
                        notificationPublisher.publish(event);
                        
                        recipient.setStatus(RecipientStatus.SENT);
                        recipient.setSentAt(Instant.now());
                    } catch (Exception ex) {
                        log.error("Failed to publish to Kafka for user {}", recipient.getUserId(), ex);
                        recipient.setStatus(RecipientStatus.FAILED);
                        recipient.setErrorMessage(ex.getMessage());
                        hasFailures = true;
                    }
                }
                
                // Bulk update the chunk
                recipientRepository.saveAll(recipientPage.getContent());
                page++;
                
            } while (recipientPage.hasNext());

            campaign.setStatus(hasFailures ? CampaignStatus.PARTIAL_FAILED : CampaignStatus.COMPLETED);
            campaignRepository.save(campaign);
            log.info("Finished processing campaign: {}, final status: {}", campaignId, campaign.getStatus());

        } catch (Exception e) {
            log.error("Critical error while processing campaign {}", campaignId, e);
            campaign.setStatus(CampaignStatus.FAILED);
            campaignRepository.save(campaign);
        }
    }
}
