package com.lms.videocourse.service.impl;

import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.videocourse.client.MultimediaClient;
import com.lms.videocourse.client.dto.MultimediaVideoResponse;
import com.lms.videocourse.client.PaymentClient;
import com.lms.videocourse.client.dto.PaymentAccessCheckResponse;
import com.lms.common.dto.ApiResponse;
import com.lms.content.common.delegate.api.FolderApiDelegate;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.response.FolderResponse;
import com.lms.content.common.dto.response.PackageResponse;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import com.lms.videocourse.dto.request.CreateVideoModuleRequest;
import com.lms.videocourse.dto.request.UpdateVideoModuleRequest;
import com.lms.videocourse.dto.response.VideoModuleResponse;
import com.lms.videocourse.dto.response.VideoWatchProgressResponse;
import com.lms.videocourse.entity.VideoModule;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.VideoModuleRepository;
import com.lms.videocourse.repository.VideoStepRepository;
import com.lms.videocourse.repository.VideoWatchProgressRepository;
import com.lms.videocourse.repository.VideoPracticeModuleProgressRepository;
import com.lms.videocourse.dto.response.VideoPracticeModuleProgressResponse;
import com.lms.videocourse.entity.VideoPracticeModuleProgress;
import com.lms.videocourse.service.IVideoModuleService;
import com.lms.common.util.SanitizationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lms.common.security.AuthPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoModuleServiceImpl implements IVideoModuleService {

    private static final String SYSTEM_TRIGGER = "system";
    private static final Set<String> PRIVILEGED_AUTHORITIES = Set.of(
            "ROLE_ADMIN",
            "ROLE_TEACHER_MANAGER",
            "ROLE_TEACHER",
            "ROLE_COLLABORATOR");


    private final VideoModuleRepository videoModuleRepository;
    private final VideoStepRepository videoStepRepository;
    private final VideoWatchProgressRepository watchProgressRepository;
    private final VideoPracticeModuleProgressRepository practiceProgressRepository;
    private final MultimediaClient multimediaClient;
    private final StudySetApiDelegate studySetApiDelegate;
    private final PaymentClient paymentClient;
    private final FolderApiDelegate folderApiDelegate;
    private final PackageApiDelegate packageApiDelegate;

    @Override
    @Transactional
    public VideoModuleResponse createVideoModule(CreateVideoModuleRequest request) {
        log.info("Creating video module: {} for step {}", request.getTitle(), request.getStepId());

        // Validate step exists
        var step = videoStepRepository.findById(request.getStepId())
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + request.getStepId()));

        // Validate module type
        com.lms.videocourse.entity.enums.ModuleType moduleType = null;
        if (request.getModuleType() != null) {
            moduleType = parseModuleType(request.getModuleType());
            // Practice modules must have contentSetId
            if (request.getContentSetId() == null || request.getContentSetId().isBlank()) {
                throw new IllegalArgumentException("Practice modules must have a contentSetId");
            }
        }

        String videoUrl = request.getVideoUrl();
        String thumbnailUrl = request.getThumbnailUrl();
        Integer duration = request.getDuration();

        // If videoCode is provided, try to sync from repo-multimedia
        if (request.getVideoCode() != null && !request.getVideoCode().isBlank()) {
            try {
                MultimediaVideoResponse multimedia = multimediaClient.getVideoByCode(request.getVideoCode());
                if (multimedia != null) {
                    if (videoUrl == null && multimedia.getPlaylistUrl() != null) {
                        videoUrl = multimedia.getPlaylistUrl();
                    }
                    if (thumbnailUrl == null && multimedia.getThumbnailPath() != null) {
                        thumbnailUrl = multimedia.getThumbnailPath();
                    }
                    if (duration == null && multimedia.getDuration() != null) {
                        duration = multimedia.getDuration();
                    }
                    log.info("Auto-populated video fields from multimedia service for code: {}",
                            request.getVideoCode());
                }
            } catch (Exception e) {
                log.warn("Could not fetch from multimedia service for code {}: {}", request.getVideoCode(),
                        e.getMessage());
                // Continue with manually provided values
            }
        }

        VideoModule module = VideoModule.builder()
                .stepId(request.getStepId())
                .moduleOrder(request.getModuleOrder())
                .title(SanitizationUtils.stripHtmlTags(request.getTitle()))
                .description(SanitizationUtils.stripHtmlTags(request.getDescription()))
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .duration(duration)
                .subtitles(request.getSubtitles())
                .videoCode(request.getVideoCode())
                .moduleType(moduleType)
                .contentSetId(request.getContentSetId())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .isActive(true)
                .build();

        module = videoModuleRepository.save(module);
        studySetApiDelegate.revertParentPackagesToDraft(step.getStudySetId(), SYSTEM_TRIGGER);
        return toResponse(module, null, null);
    }

    @Override
    public VideoModuleResponse getVideoModuleById(String id, String userId) {
        VideoModule module = videoModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video module not found: " + id));
                
        var step = videoStepRepository.findById(module.getStepId())
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found"));
        validateAccess(step.getStudySetId());

        VideoWatchProgressResponse watchProgress = getWatchProgressForUser(userId, id);
        VideoPracticeModuleProgressResponse practiceProgress = getPracticeProgressForUser(userId, id);
        return toResponse(module, watchProgress, practiceProgress);
    }

    @Override
    public List<VideoModuleResponse> getVideoModulesByStepId(String stepId, String userId) {
        var step = videoStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + stepId));
        validateAccess(step.getStudySetId());

        return videoModuleRepository.findByStepIdAndIsActiveTrueOrderByModuleOrderAsc(stepId)
                .stream()
                .map(module -> {
                    VideoWatchProgressResponse watchProgress = getWatchProgressForUser(userId, module.getId());
                    VideoPracticeModuleProgressResponse practiceProgress = getPracticeProgressForUser(userId,
                            module.getId());
                    return toResponse(module, watchProgress, practiceProgress);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VideoModuleResponse updateVideoModule(String id, UpdateVideoModuleRequest request) {
        VideoModule module = videoModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video module not found: " + id));

        if (request.getTitle() != null)
            module.setTitle(SanitizationUtils.stripHtmlTags(request.getTitle()));
        if (request.getDescription() != null)
            module.setDescription(SanitizationUtils.stripHtmlTags(request.getDescription()));
        if (request.getModuleOrder() != null)
            module.setModuleOrder(request.getModuleOrder());
        if (request.getVideoUrl() != null)
            module.setVideoUrl(request.getVideoUrl());
        if (request.getThumbnailUrl() != null)
            module.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getDuration() != null)
            module.setDuration(request.getDuration());
        if (request.getSubtitles() != null)
            module.setSubtitles(request.getSubtitles());
        if (request.getVideoCode() != null)
            module.setVideoCode(request.getVideoCode());
        if (request.getModuleType() != null) {
            com.lms.videocourse.entity.enums.ModuleType type = parseModuleType(request.getModuleType());
            module.setModuleType(type);
        }
        if (request.getContentSetId() != null)
            module.setContentSetId(request.getContentSetId());
        if (request.getIsRequired() != null)
            module.setIsRequired(request.getIsRequired());
        if (request.getIsActive() != null)
            module.setIsActive(request.getIsActive());

        module = videoModuleRepository.save(module);
        String studySetId = videoStepRepository.findById(module.getStepId())
                .map(s -> s.getStudySetId())
                .orElse(null);
        studySetApiDelegate.revertParentPackagesToDraft(studySetId, SYSTEM_TRIGGER);
        return toResponse(module, null, null);
    }

    @Override
    @Transactional
    public void deleteVideoModule(String id) {
        VideoModule module = videoModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video module not found: " + id));
        String studySetId = videoStepRepository.findById(module.getStepId())
                .map(s -> s.getStudySetId())
                .orElse(null);
        module.setIsActive(false);
        videoModuleRepository.save(module);
        studySetApiDelegate.revertParentPackagesToDraft(studySetId, SYSTEM_TRIGGER);
    }

    private VideoWatchProgressResponse getWatchProgressForUser(String userId, String moduleId) {
        if (userId == null)
            return null;
        return watchProgressRepository.findByUserIdAndVideoModuleId(userId, moduleId)
                .map(p -> VideoWatchProgressResponse.builder()
                        .id(p.getId())
                        .videoModuleId(p.getVideoModuleId())
                        .stepId(p.getStepId())
                        .status(p.getStatus())
                        .watchedSeconds(p.getWatchedSeconds())
                        .totalDurationSeconds(p.getTotalDurationSeconds())
                        .watchPercent(p.getWatchPercent())
                        .lastPositionSeconds(p.getLastPositionSeconds())
                        .autoCompleted(p.getAutoCompleted())
                        .firstStartedAt(p.getFirstStartedAt())
                        .lastWatchedAt(p.getLastWatchedAt())
                        .completedAt(p.getCompletedAt())
                        .build())
                .orElse(null);
    }

    private VideoPracticeModuleProgressResponse getPracticeProgressForUser(String userId, String moduleId) {
        if (userId == null)
            return null;
        return practiceProgressRepository.findByUserIdAndVideoModuleId(userId, moduleId)
                .map(p -> VideoPracticeModuleProgressResponse.builder()
                        .id(p.getId())
                        .videoModuleId(p.getVideoModuleId())
                        .stepId(p.getStepId())
                        .studySetId(p.getStudySetId())
                        .moduleType(p.getModuleType())
                        .contentSetId(p.getContentSetId())
                        .status(p.getStatus())
                        .progressPercentage(p.getProgressPercentage())
                        .firstStartedAt(p.getFirstStartedAt())
                        .completedAt(p.getCompletedAt())
                        .build())
                .orElse(null);
    }

    private VideoModuleResponse toResponse(VideoModule module, VideoWatchProgressResponse watchProgress,
            VideoPracticeModuleProgressResponse practiceProgress) {
        return VideoModuleResponse.builder()
                .id(module.getId())
                .stepId(module.getStepId())
                .moduleOrder(module.getModuleOrder())
                .title(module.getTitle())
                .description(module.getDescription())
                .videoUrl(module.getVideoUrl())
                .thumbnailUrl(module.getThumbnailUrl())
                .duration(module.getDuration())
                .subtitles(module.getSubtitles())
                .videoCode(module.getVideoCode())
                .moduleType(module.getModuleType())
                .contentSetId(module.getContentSetId())
                .isRequired(module.getIsRequired())
                .isActive(module.getIsActive())
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .watchProgress(watchProgress)
                .practiceProgress(practiceProgress)
                .build();
    }

    private com.lms.videocourse.entity.enums.ModuleType parseModuleType(String typeStr) {
        if (typeStr == null || typeStr.isBlank())
            return null;
        String type = typeStr.trim().toUpperCase();

        // Handle legacy/alias mapping
        if ("KANJI".equals(type))
            return com.lms.videocourse.entity.enums.ModuleType.KANJI_ORIGIN;
        if ("LISTENING_PRACTICE".equals(type))
            return com.lms.videocourse.entity.enums.ModuleType.LISTENING;

        try {
            com.lms.videocourse.entity.enums.ModuleType result = com.lms.videocourse.entity.enums.ModuleType
                    .valueOf(type);
            // Additionally check if it's one of the 6 allowed types
            List<com.lms.videocourse.entity.enums.ModuleType> allowed = List.of(
                    com.lms.videocourse.entity.enums.ModuleType.FLASHCARD,
                    com.lms.videocourse.entity.enums.ModuleType.QUIZ,
                    com.lms.videocourse.entity.enums.ModuleType.LISTENING,
                    com.lms.videocourse.entity.enums.ModuleType.WRITING,
                    com.lms.videocourse.entity.enums.ModuleType.KANJI_ORIGIN,
                    com.lms.videocourse.entity.enums.ModuleType.PRONUNCIATION);
            if (!allowed.contains(result)) {
                throw new IllegalArgumentException("Invalid practice module type: " + typeStr);
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid practice module type: " + typeStr);
        }
    }

    private void validateAccess(String studySetId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) 
                ? principal.userId() : "anonymous";

        log.info("[ACCESS_CHECK] Validating access for user: {} to studySet: {}", currentUserId, studySetId);

        if (hasPrivilegedAuthority()) {
            log.info("[ACCESS_CHECK] User {} has privileged authority, bypassing check", currentUserId);
            return;
        }

        PackageResponse pkg;
        try {
            pkg = resolvePackageForStudySet(studySetId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ACCESS_CHECK] Failed to resolve package for study set {}: {}", studySetId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn cần mua khóa học để truy cập nội dung này.");
        }
        if (isFreePackage(pkg)) {
            log.info("[ACCESS_CHECK] Course is FREE, granting access to user: {}", currentUserId);
            return;
        }

        String checkId = pkg.getId();

        if ("anonymous".equals(currentUserId)) {
            log.warn("[ACCESS_CHECK] Anonymous user denied access to PAID course: {}", checkId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để truy cập nội dung này.");
        }

        log.info("[ACCESS_CHECK] PackageId (CheckId): {}", checkId);

        try {
            ApiResponse<PaymentAccessCheckResponse> response = paymentClient.checkAccess(checkId);
            boolean hasAccess = (response != null && response.data() != null && response.data().isHasAccess());
            log.info("[ACCESS_CHECK] Payment service response: hasAccess={}", hasAccess);

            if (!hasAccess) {
                log.warn("[ACCESS_CHECK] User {} DENIED access to package {}", currentUserId, checkId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn cần mua khóa học để truy cập nội dung này.");
            }
            log.info("[ACCESS_CHECK] User {} GRANTED access to package {}", currentUserId, checkId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ACCESS_CHECK] Error calling payment service for package {}: {}", checkId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không thể xác minh quyền truy cập khóa học. Vui lòng thử lại sau.");
        }
    }

    private PackageResponse resolvePackageForStudySet(String studySetId) {
        List<FolderResponse> folders = folderApiDelegate.getFoldersByStudySetId(studySetId);
        if (folders == null || folders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Bạn cần mua khóa học để truy cập nội dung này.");
        }

        String packageId = folders.stream()
                .map(FolderResponse::getPackageId)
                .filter(StringUtils::hasText)
                .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Bạn cần mua khóa học để truy cập nội dung này."));

        PackageResponse pkg = packageApiDelegate.getPackageById(packageId);
        if (pkg == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Bạn cần mua khóa học để truy cập nội dung này.");
        }
        return pkg;
    }

    private boolean isFreePackage(PackageResponse pkg) {
        if (pkg == null) {
            return false;
        }
        if (StringUtils.hasText(pkg.getPricingType()) && "FREE".equalsIgnoreCase(pkg.getPricingType())) {
            return true;
        }
        return pkg.getPrice() != null && pkg.getPrice().compareTo(BigDecimal.ZERO) <= 0;
    }

    private boolean hasPrivilegedAuthority() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(PRIVILEGED_AUTHORITIES::contains);
    }
}
