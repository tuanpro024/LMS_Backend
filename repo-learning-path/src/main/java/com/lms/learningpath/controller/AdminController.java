package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.learningpath.service.SetUnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SetUnlockService setUnlockService;

    /**
     * Manually unlock a study set for a user
     */
    @PostMapping("/users/{userId}/unlock-set/{studySetId}")
    public ResponseEntity<ApiResponse<Void>> unlockSetForUser(
            @PathVariable String userId,
            @PathVariable String studySetId
    ) {
        setUnlockService.unlockSetForUser(userId, studySetId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}