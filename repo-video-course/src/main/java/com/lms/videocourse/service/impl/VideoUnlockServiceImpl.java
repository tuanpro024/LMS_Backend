package com.lms.videocourse.service.impl;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.delegate.api.FolderApiDelegate;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.response.FolderResponse;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.videocourse.client.PaymentClient;
import com.lms.videocourse.client.dto.PaymentAccessCheckResponse;
import com.lms.videocourse.entity.VideoStep;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.VideoStepRepository;
import com.lms.videocourse.service.IVideoUnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoUnlockServiceImpl implements IVideoUnlockService {

    private final VideoStepRepository videoStepRepository;
    private final FolderApiDelegate folderApiDelegate;
    private final PackageApiDelegate packageApiDelegate;
    private final PaymentClient paymentClient;

    @Override
    public boolean isStepUnlocked(String userId, String stepId) {
        VideoStep step = videoStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + stepId));
                
        PackageResponse pkg;
        try {
            pkg = resolvePackageForStudySet(step.getStudySetId());
        } catch (Exception e) {
            log.error("Failed to resolve package for study set {}: {}", step.getStudySetId(), e.getMessage());
            return false;
        }
        if (isFreePackage(pkg)) {
            return true;
        }

        String checkId = pkg.getId();

        try {
            ApiResponse<PaymentAccessCheckResponse> response = paymentClient.checkAccess(checkId);
            if (response != null && response.data() != null) {
                return response.data().isHasAccess();
            }
        } catch (Exception e) {
            log.error("Failed to check access via PaymentClient for packageId {}", checkId, e);
        }
        
        return false;
    }

    @Override
    public String getLockReason(String userId, String stepId) {
        if (!isStepUnlocked(userId, stepId)) {
            return "PACKAGE_NOT_PURCHASED";
        }
        return null;
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
}
