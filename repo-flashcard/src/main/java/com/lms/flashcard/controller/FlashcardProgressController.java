package com.lms.flashcard.controller;

import com.lms.flashcard.dto.response.FlashcardStudySetProgressResponse;
import com.lms.flashcard.service.FlashcardProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/study-sets")
@RequiredArgsConstructor
public class FlashcardProgressController {

    private final FlashcardProgressService progressService;

    @GetMapping("/{studySetId}/progress")
    public FlashcardStudySetProgressResponse getStudySetProgress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String studySetId) {
        String userId = jwt.getSubject();
        return progressService.getStudySetProgress(userId, studySetId);
    }
}
