package com.lms.multimedia.controller;

import com.lms.multimedia.dto.request.SubtitleWebhookRequest;
import com.lms.multimedia.dto.response.SubtitleResponse;
import com.lms.multimedia.service.SubtitleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subtitles")
@RequiredArgsConstructor
@Slf4j
public class SubtitleController {

    private final SubtitleService subtitleService;

    /**
     * Webhook endpoint - called by Express after subtitle upload
     * 
     * @param request Webhook data from Express
     * @return Created subtitle metadata
     */
    @PostMapping("/webhook")
    public ResponseEntity<SubtitleResponse> handleSubtitleWebhook(
            @RequestBody SubtitleWebhookRequest request) {

        log.info("Subtitle webhook received - videoCode: {}, name: {}",
                request.getVideoCode(), request.getSubtitleName());

        SubtitleResponse response = subtitleService.handleWebhook(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all subtitles for a video
     * 
     * @param videoId Video ID
     * @return List of subtitles
     */
    @GetMapping("/video/{videoId}")
    public ResponseEntity<List<SubtitleResponse>> getSubtitlesByVideo(@PathVariable String videoId) {
        log.info("Get subtitles for video: {}", videoId);
        List<SubtitleResponse> subtitles = subtitleService.getSubtitlesByVideoId(videoId);
        return ResponseEntity.ok(subtitles);
    }

    /**
     * Get the active subtitle for a video
     * 
     * @param videoId Video ID
     * @return Active subtitle or 404
     */
    @GetMapping("/video/{videoId}/active")
    public ResponseEntity<SubtitleResponse> getActiveSubtitle(@PathVariable String videoId) {
        log.info("Get active subtitle for video: {}", videoId);
        return subtitleService.getActiveSubtitle(videoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Set a subtitle as active
     * 
     * @param subtitleId Subtitle ID
     * @return Updated subtitle
     */
    @PutMapping("/{subtitleId}/activate")
    public ResponseEntity<SubtitleResponse> setActiveSubtitle(@PathVariable String subtitleId) {
        log.info("Set active subtitle: {}", subtitleId);
        SubtitleResponse response = subtitleService.setActiveSubtitle(subtitleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft delete a subtitle
     * 
     * @param subtitleId Subtitle ID
     * @return No content
     */
    @DeleteMapping("/{subtitleId}")
    public ResponseEntity<Void> deleteSubtitle(@PathVariable String subtitleId) {
        log.info("Delete subtitle: {}", subtitleId);
        subtitleService.deleteSubtitle(subtitleId);
        return ResponseEntity.noContent().build();
    }
}
