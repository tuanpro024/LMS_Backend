package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.videocourse.dto.response.AvailableModuleResponse;
import com.lms.videocourse.service.IAvailableModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for browsing available study set modules from other microservices.
 * Used by Admin/Teacher when associating flashcard/writing/kanji/quiz study
 * sets
 * with video steps (for related practice after watching a video).
 * Only accessible to Admin and Teacher roles.
 */
@RestController
@RequestMapping("/admin/available-modules")
@RequiredArgsConstructor
public class AvailableModuleController {

    private final IAvailableModuleService availableModuleService;

    /** Get all available study sets from all repos */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAllAvailableModules(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(availableModuleService.getAllAvailableModules(q)));
    }

    /** Get available flashcard study sets */
    @GetMapping("/flashcard")
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAvailableFlashcardSets(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(availableModuleService.getAvailableFlashcardSets(q)));
    }

    /** Get available writing study sets */
    @GetMapping("/writing")
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAvailableWritingSets(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(availableModuleService.getAvailableWritingSets(q)));
    }

    /** Get available kanji study sets */
    @GetMapping({"/kanji", "/kanji-origin"})
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAvailableKanjiSets(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(availableModuleService.getAvailableKanjiSets(q)));
    }

    /** Get available quiz study sets */
    @GetMapping("/quiz")
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAvailableQuizSets(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(availableModuleService.getAvailableQuizSets(q)));
    }

    /** Get available listening practice study sets */
    @GetMapping("/listening")
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAvailableListeningSets(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(availableModuleService.getAvailableListeningSets(q)));
    }

    /** Get available pronunciation study sets */
    @GetMapping("/pronunciation")
    public ResponseEntity<ApiResponse<List<AvailableModuleResponse>>> getAvailablePronunciationSets(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(availableModuleService.getAvailablePronunciationSets(q)));
    }
}
