package com.lms.aipractice.controller;

import com.lms.aipractice.dto.request.CreateAttemptRequest;
import com.lms.aipractice.dto.request.SubmitAnswerRequest;
import com.lms.aipractice.dto.response.AttemptResponse;
import com.lms.aipractice.dto.response.GradingJobResponse;
import com.lms.aipractice.dto.response.GradingResultResponse;
import com.lms.aipractice.service.AiPracticeAttemptService;
import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AiPracticeAttemptController {

    private final AiPracticeAttemptService attemptService;

    // ── Attempt lifecycle ─────────────────────────────────────────────

    @PostMapping("/attempts")
    public ResponseEntity<ApiResponse<AttemptResponse>> createAttempt(
            @Valid @RequestBody CreateAttemptRequest request,
            Authentication auth) {
        String userId = ((AuthPrincipal) auth.getPrincipal()).userId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(attemptService.createAttempt(request, userId)));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<ApiResponse<AttemptResponse>> getAttempt(
            @PathVariable String attemptId, Authentication auth) {
        String userId = ((AuthPrincipal) auth.getPrincipal()).userId();
        return ResponseEntity.ok(ApiResponse.ok(attemptService.getAttempt(attemptId, userId)));
    }

    @GetMapping("/attempts/latest")
    public ResponseEntity<ApiResponse<List<AttemptResponse>>> getLatestAttemptsByStudySetIds(
            @RequestParam("studySetIds") List<String> studySetIds,
            Authentication auth) {
        String userId = ((AuthPrincipal) auth.getPrincipal()).userId();
        return ResponseEntity.ok(ApiResponse.ok(attemptService.getLatestAttemptsByStudySetIds(studySetIds, userId)));
    }

    @GetMapping("/attempts/history")
    public ResponseEntity<ApiResponse<List<AttemptResponse>>> getAttemptHistory(
            @RequestParam("studySetId") String studySetId,
            Authentication auth) {
        String userId = ((AuthPrincipal) auth.getPrincipal()).userId();
        return ResponseEntity.ok(ApiResponse.ok(attemptService.getAttemptHistory(studySetId, userId)));
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<ApiResponse<AttemptResponse>> submitAttempt(
            @PathVariable String attemptId, Authentication auth) {
        String userId = ((AuthPrincipal) auth.getPrincipal()).userId();
        return ResponseEntity.ok(ApiResponse.ok(attemptService.submitAttempt(attemptId, userId)));
    }

    // ── Answer submission — text ──────────────────────────────────────

    @PostMapping("/attempts/{attemptId}/answers")
    public ResponseEntity<ApiResponse<AttemptResponse>> submitTextAnswer(
            @PathVariable String attemptId,
            @Valid @RequestBody SubmitAnswerRequest request,
            Authentication auth) {
        String userId = ((AuthPrincipal) auth.getPrincipal()).userId();
        return ResponseEntity.ok(ApiResponse.ok(attemptService.submitAnswer(attemptId, request, userId)));
    }

    // ── Answer submission — audio (multipart for speaking/audio_compare) ──

    @PostMapping("/attempts/{attemptId}/answers/audio")
    public ResponseEntity<ApiResponse<AttemptResponse>> submitAudioAnswer(
            @PathVariable String attemptId,
            @RequestParam("itemId") String itemId,
            @RequestParam("audio") MultipartFile audioFile,
            Authentication auth) throws IOException {

        String userId = ((AuthPrincipal) auth.getPrincipal()).userId();

        // Save audio to temp storage
        Path uploadDir = Path.of(System.getProperty("java.io.tmpdir"), "ai-practice-audio");
        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID() + "_" + audioFile.getOriginalFilename();
        Path savedPath = uploadDir.resolve(filename);
        Files.copy(audioFile.getInputStream(), savedPath, StandardCopyOption.REPLACE_EXISTING);

        SubmitAnswerRequest request = new SubmitAnswerRequest();
        request.setItemId(itemId);
        request.setAnswerAudioPath(savedPath.toAbsolutePath().toString());

        return ResponseEntity.ok(ApiResponse.ok(attemptService.submitAnswer(attemptId, request, userId)));
    }

    // ── Results ───────────────────────────────────────────────────────

    @GetMapping("/attempts/{attemptId}/results")
    public ResponseEntity<ApiResponse<List<GradingResultResponse>>> getResults(
            @PathVariable String attemptId, Authentication auth) {
        String userId = ((AuthPrincipal) auth.getPrincipal()).userId();
        return ResponseEntity.ok(ApiResponse.ok(attemptService.getResults(attemptId, userId)));
    }

    // ── Admin: job monitoring ─────────────────────────────────────────

    @GetMapping("/grading-jobs/{jobId}")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<GradingJobResponse>> getJob(@PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.ok(attemptService.getJob(jobId)));
    }
}
