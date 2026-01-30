package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.request.CreateQuestRequest;
import com.lms.learningpath.dto.response.QuestResponse;
import com.lms.learningpath.service.QuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quests")
@RequiredArgsConstructor
@Slf4j
public class QuestController {

    private final QuestService questService;

    /**
     * Create a new quest (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestResponse>> createQuest(
            @RequestBody @Valid CreateQuestRequest request
    ) {
        QuestResponse response = questService.createQuest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get active quests for current user
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<QuestResponse>>> getActiveQuests(
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        List<QuestResponse> response = questService.getActiveQuests(principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Claim quest reward
     */
    @PostMapping("/{questId}/claim")
    public ResponseEntity<ApiResponse<QuestResponse>> claimQuest(
            @PathVariable String questId,
            @RequestParam String period,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        QuestResponse response = questService.claimQuest(principal.userId(), questId, period);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}