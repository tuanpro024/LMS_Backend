package com.lms.notification.service;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.common.event.PackageStatusEvent;
import com.lms.notification.client.IdentityClient;
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

        private static final int BATCH_SIZE = 1000;

        private final ManualNotificationCampaignRepository campaignRepository;
        private final ManualNotificationRecipientRepository recipientRepository;
        private final CampaignProcessorService processorService;
        private final IdentityClient identityClient;

        @Transactional
        public CampaignResponse sendNow(SendCampaignRequest request, String createdBy) {
                log.info("Creating manual notification campaign for {} users by {}", request.getUserIds().size(),
                                createdBy);

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

        @Transactional
        public CampaignResponse sendPackageDraftNotificationToAllUsers(PackageStatusEvent event) {
                String packageName = sanitizePackageName(event.packageName());
                String moduleName = normalizeModuleName(event.moduleType());

                String title = "Goi hoc tam dung de cap nhat";
                String message = "Goi hoc '" + packageName + "' cua module " + moduleName
                                + " dang tam dung de sua. Tien trinh hoc cua ban da duoc luu."
                                + " Vui long quay lai sau khi goi hoc duoc publish lai.";

                ManualNotificationCampaign campaign = ManualNotificationCampaign.builder()
                                .title(title)
                                .message(message)
                                .audienceType(AudienceType.ALL)
                                .status(CampaignStatus.PROCESSING)
                                .createdBy(sanitizeActor(event.changedBy()))
                                .build();

                campaign = campaignRepository.save(campaign);
                String campaignId = campaign.getId();

                int page = 0;
                long totalRecipients = 0;
                while (true) {
                        ApiResponse<PageResponse<String>> response = identityClient.getActiveUserIds(page, BATCH_SIZE);
                        PageResponse<String> pageData = response == null ? null : response.data();
                        if (pageData == null || pageData.getItems() == null || pageData.getItems().isEmpty()) {
                                break;
                        }

                        List<ManualNotificationRecipient> recipients = pageData.getItems().stream()
                                        .distinct()
                                        .map(userId -> ManualNotificationRecipient.builder()
                                                        .campaignId(campaignId)
                                                        .userId(userId)
                                                        .status(RecipientStatus.PENDING)
                                                        .build())
                                        .collect(Collectors.toList());

                        recipientRepository.saveAll(recipients);
                        totalRecipients += recipients.size();

                        page++;
                        if (page >= pageData.getTotalPages()) {
                                break;
                        }
                }

                if (totalRecipients == 0) {
                        campaign.setStatus(CampaignStatus.COMPLETED);
                        campaignRepository.save(campaign);
                        log.warn("No active recipients found for package draft notification campaign {}", campaignId);
                        return mapToResponse(campaign);
                }

                processorService.processSpecificUsersCampaign(campaignId);
                log.info("Created package draft campaign {} for packageId={} with {} recipients",
                                campaignId, event.packageId(), totalRecipients);
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

        private String sanitizePackageName(String packageName) {
                if (packageName == null || packageName.isBlank()) {
                        return "Khong ro ten goi";
                }
                return packageName.trim();
        }

        private String sanitizeActor(String actor) {
                if (actor == null || actor.isBlank()) {
                        return "system";
                }
                return actor.trim();
        }

        private String normalizeModuleName(String moduleType) {
                if (moduleType == null || moduleType.isBlank()) {
                        return "Unknown";
                }

                return switch (moduleType.trim().toUpperCase()) {
                        case "FLASHCARD" -> "Flashcard";
                        case "WRITING" -> "Writing";
                        case "KANJI_ORIGIN" -> "Kanji Origin";
                        case "PRONUNCIATION" -> "Pronunciation";
                        case "QUIZ" -> "Quiz";
                        case "LISTENING_PRACTICE" -> "Listening Practice";
                        case "VIDEO_COURSE" -> "Video Course";
                        case "AI_PRACTICE" -> "AI Practice";
                        default -> moduleType.replace('_', ' ').trim();
                };
        }
}
