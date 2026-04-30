package com.lms.flashcard.controller;

import com.lms.common.security.AuthPrincipal;
import com.lms.flashcard.dto.response.FlashcardStudySetProgressResponse;
import com.lms.flashcard.service.FlashcardProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/study-sets")
@RequiredArgsConstructor
public class FlashcardProgressController {

    private final FlashcardProgressService progressService;

    @GetMapping("/{studySetId}/progress")
    public FlashcardStudySetProgressResponse getStudySetProgress(
            Authentication authentication,
            @PathVariable String studySetId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        String userId = principal.userId();
        return progressService.getStudySetProgress(userId, studySetId);
    }

    @GetMapping("/history")
    public List<FlashcardStudySetProgressResponse> getUserStudySetHistory(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return progressService.getUserStudySetHistory(principal.userId());
    }
}
