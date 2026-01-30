package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.response.SetProgressResponse;
import com.lms.learningpath.entity.UserSetProgress;
import com.lms.learningpath.entity.enums.SetStatus;
import com.lms.learningpath.repository.UserSetProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    private final UserSetProgressRepository setProgressRepository;

    /**
     * Get user's learning progress overview
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProgressOverview(
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        String userId = principal.userId();

        // Get all user's set progress
        List<UserSetProgress> allProgress = setProgressRepository.findByUserId(userId);

        // Calculate statistics
        long totalSets = allProgress.size();
        long completedSets = setProgressRepository.countByUserIdAndStatus(userId, SetStatus.COMPLETED);
        long inProgressSets = setProgressRepository.countByUserIdAndStatus(userId, SetStatus.IN_PROGRESS);

        int totalExp = allProgress.stream()
                .mapToInt(UserSetProgress::getEarnedExp)
                .sum();

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalSets", totalSets);
        overview.put("completedSets", completedSets);
        overview.put("inProgressSets", inProgressSets);
        overview.put("totalExp", totalExp);

        return ResponseEntity.ok(ApiResponse.ok(overview));
    }

    /**
     * Get all user's set progress
     */
    @GetMapping("/sets")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllSetProgress(
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        List<UserSetProgress> progressList = setProgressRepository.findByUserId(principal.userId());

        List<Map<String, Object>> response = progressList.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("studySetId", p.getStudySetId());
                    map.put("status", p.getStatus());
                    map.put("completedModules", p.getCompletedModules());
                    map.put("totalModules", p.getTotalModules());
                    map.put("bestScore", p.getBestScore());
                    map.put("earnedExp", p.getEarnedExp());
                    map.put("completedAt", p.getCompletedAt());
                    map.put("lastStudiedAt", p.getLastStudiedAt());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}