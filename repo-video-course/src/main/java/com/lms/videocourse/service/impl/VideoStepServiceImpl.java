package com.lms.videocourse.service.impl;

import com.lms.videocourse.client.PaymentClient;
import com.lms.videocourse.client.dto.PaymentAccessCheckResponse;
import com.lms.common.dto.ApiResponse;
import com.lms.content.common.delegate.api.FolderApiDelegate;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.response.FolderResponse;
import com.lms.content.common.dto.response.PackageResponse;
import java.math.BigDecimal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.videocourse.dto.request.CreateVideoStepRequest;
import com.lms.videocourse.dto.request.UpdateVideoStepRequest;
import com.lms.videocourse.dto.response.VideoStepProgressResponse;
import com.lms.videocourse.dto.response.VideoStepResponse;
import com.lms.videocourse.entity.VideoStep;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.VideoStepRepository;
import com.lms.videocourse.repository.VideoModuleRepository;
import com.lms.videocourse.service.IVideoStepService;
import com.lms.videocourse.service.IVideoUnlockService;
import com.lms.videocourse.service.IVideoProgressService;
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
public class VideoStepServiceImpl implements IVideoStepService {

    private static final String SYSTEM_TRIGGER = "system";
    private static final Set<String> PRIVILEGED_AUTHORITIES = Set.of(
            "ROLE_ADMIN",
            "ROLE_TEACHER_MANAGER",
            "ROLE_TEACHER",
            "ROLE_COLLABORATOR");

    private final VideoStepRepository videoStepRepository;
    private final VideoModuleRepository videoModuleRepository;
    private final IVideoUnlockService unlockService;
    private final IVideoProgressService progressService;
    private final StudySetApiDelegate studySetApiDelegate;
    private final PaymentClient paymentClient;
    private final FolderApiDelegate folderApiDelegate;
    private final PackageApiDelegate packageApiDelegate;

    @Override
    @Transactional
    public VideoStepResponse createVideoStep(CreateVideoStepRequest request) {
        log.info("Creating video step: {} for studySet {}", request.getTitle(), request.getStudySetId());

        // Validate studySet exists (using videoCourseRepository for now if it's the
        // source, but we
        // should ideally check studySet)
        // Since we are removing VideoCourse, we bypass the course check.
        // We'll trust the studySetId provided for now or implement a different check.

        VideoStep step = VideoStep.builder()
                .studySetId(request.getStudySetId())
                .title(SanitizationUtils.stripHtmlTags(request.getTitle()))
                .description(SanitizationUtils.stripHtmlTags(request.getDescription()))
                .stepOrder(request.getStepOrder())
                .icon(request.getIcon())
                .color(request.getColor())
                .estimatedMinutes(request.getEstimatedMinutes())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .isActive(true)
                .build();

        step = videoStepRepository.save(step);
        studySetApiDelegate.revertParentPackagesToDraft(request.getStudySetId(), SYSTEM_TRIGGER);

        int moduleCount = (int) videoModuleRepository.countByStepIdAndIsActiveTrue(step.getId());
        return toResponse(step, null, null, null, moduleCount);
    }

    @Override
    public VideoStepResponse getVideoStepById(String id) {
        VideoStep step = videoStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + id));
        validateAccess(step.getStudySetId());
        int moduleCount = (int) videoModuleRepository.countByStepIdAndIsActiveTrue(step.getId());
        return toResponse(step, null, null, null, moduleCount);
    }

    @Override
    public List<VideoStepResponse> getVideoStepsByCourseId(String studySetId, String userId) {
        validateAccess(studySetId);

        List<VideoStep> steps = videoStepRepository
                .findByStudySetIdAndIsActiveTrueOrderByStepOrderAsc(studySetId);

        return steps.stream().map(step -> {
            Boolean isUnlocked = null;
            String lockReason = null;
            VideoStepProgressResponse progress = null;

            if (userId != null) {
                isUnlocked = unlockService.isStepUnlocked(userId, step.getId());
                if (!isUnlocked) {
                    lockReason = unlockService.getLockReason(userId, step.getId());
                }
                progress = progressService.getStepProgress(userId, step.getId());
            }
            int moduleCount = (int) videoModuleRepository.countByStepIdAndIsActiveTrue(step.getId());
            return toResponse(step, isUnlocked, lockReason, progress, moduleCount);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VideoStepResponse updateVideoStep(String id, UpdateVideoStepRequest request) {
        VideoStep step = videoStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + id));

        if (request.getTitle() != null)
            step.setTitle(SanitizationUtils.stripHtmlTags(request.getTitle()));
        if (request.getDescription() != null)
            step.setDescription(SanitizationUtils.stripHtmlTags(request.getDescription()));
        if (request.getStepOrder() != null)
            step.setStepOrder(request.getStepOrder());
        if (request.getIcon() != null)
            step.setIcon(request.getIcon());
        if (request.getColor() != null)
            step.setColor(request.getColor());
        if (request.getEstimatedMinutes() != null)
            step.setEstimatedMinutes(request.getEstimatedMinutes());
        if (request.getIsRequired() != null)
            step.setIsRequired(request.getIsRequired());
        if (request.getIsActive() != null)
            step.setIsActive(request.getIsActive());

        step = videoStepRepository.save(step);
        studySetApiDelegate.revertParentPackagesToDraft(step.getStudySetId(), SYSTEM_TRIGGER);
        int moduleCount = (int) videoModuleRepository.countByStepIdAndIsActiveTrue(step.getId());
        return toResponse(step, null, null, null, moduleCount);
    }

    @Override
    @Transactional
    public void deleteVideoStep(String id) {
        VideoStep step = videoStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + id));
        step.setIsActive(false);
        videoStepRepository.save(step);
        studySetApiDelegate.revertParentPackagesToDraft(step.getStudySetId(), SYSTEM_TRIGGER);
    }

    private VideoStepResponse toResponse(VideoStep step, Boolean isUnlocked, String lockReason,
            VideoStepProgressResponse progress, int moduleCount) {
        return VideoStepResponse.builder()
                .id(step.getId())
                .studySetId(step.getStudySetId())
                .title(step.getTitle())
                .description(step.getDescription())
                .stepOrder(step.getStepOrder())
                .icon(step.getIcon())
                .color(step.getColor())
                .estimatedMinutes(step.getEstimatedMinutes())
                .isRequired(step.getIsRequired())
                .isActive(step.getIsActive())
                .createdAt(step.getCreatedAt())
                .updatedAt(step.getUpdatedAt())
                .isUnlocked(isUnlocked)
                .lockReason(lockReason)
                .moduleCount(moduleCount)
                .progress(progress)
                .build();
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

        log.info("[ACCESS_CHECK] InternalPackageId (CheckId): {}", checkId);

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
