package com.lms.flashcard.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.flashcard.dto.request.AddCardsToStudySetRequest;
import com.lms.flashcard.dto.request.UpdateCardStatusRequest;
import com.lms.flashcard.dto.response.CardResponse;
import com.lms.flashcard.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.lms.common.security.AuthPrincipal;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<CardResponse>>> addCardsToStudySet(
            @RequestBody @Valid AddCardsToStudySetRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        List<CardResponse> responses = cardService.addCardsToStudySet(
                request.getStudySetId(),
                request.getCards(),
                principal.userId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(responses));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateCardStatus(
            @PathVariable String id,
            @RequestBody @Valid UpdateCardStatusRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        cardService.updateCardStatus(principal.userId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
