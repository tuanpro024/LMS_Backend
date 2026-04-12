package com.lms.notification.service;

import com.lms.common.dto.PageResponse;
import com.lms.notification.dto.request.SendCampaignRequest;
import com.lms.notification.dto.response.CampaignResponse;
import com.lms.notification.entity.ManualNotificationCampaign;
import com.lms.notification.entity.ManualNotificationRecipient;
import com.lms.notification.entity.enums.AudienceType;
import com.lms.notification.entity.enums.CampaignStatus;
import com.lms.notification.entity.enums.RecipientStatus;
import com.lms.notification.repository.ManualNotificationCampaignRepository;
import com.lms.notification.repository.ManualNotificationRecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService {

    private final ManualNotificationCampaignRepository campaignRepository;
    private final ManualNotificationRecipientRepository recipientRepository;
    private final CampaignProcessorService processorService;

    @Transactional
    public CampaignResponse sendNow(SendCampaignRequest request, String createdBy) {
        log.info("Creating manual notification campaign for {} users by {}", request.getUserIds().size(), createdBy);

        ManualNotificationCampaign campaign = ManualNotificationCampaign.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .audienceType(AudienceType.SPECIFIC_USERS)
                .status(CampaignStatus.PROCESSING)
                .createdBy(createdBy)
                .build();
        
        campaign = campaignRepository.save(campaign);
        final String campaignId = campaign.getId();

        List<ManualNotificationRecipient> recipients = request.getUserIds().stream()
                .distinct()
                .map(userId -> ManualNotificationRecipient.builder()
                        .campaignId(campaignId)
                        .userId(userId)
                        .status(RecipientStatus.PENDING)
                        .build())
                .collect(Collectors.toList());

        recipientRepository.saveAll(recipients);

        // Trigger Async processing
        processorService.processSpecificUsersCampaign(campaignId);

        return mapToResponse(campaign);
    }

    public PageResponse<CampaignResponse> listCampaigns(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ManualNotificationCampaign> campaignPage = campaignRepository.findAll(pageable);
        
        List<CampaignResponse> items = campaignPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<CampaignResponse>builder()
                .items(items)
                .totalElements(campaignPage.getTotalElements())
                .totalPages(campaignPage.getTotalPages())
                .page(page)
                .size(size)
                .build();
    }

    private CampaignResponse mapToResponse(ManualNotificationCampaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .message(campaign.getMessage())
                .audienceType(campaign.getAudienceType())
                .status(campaign.getStatus())
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}
