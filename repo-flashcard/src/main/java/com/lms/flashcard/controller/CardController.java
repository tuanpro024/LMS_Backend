package com.lms.flashcard.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.flashcard.dto.request.AddCardsToStudySetRequest;
import com.lms.flashcard.dto.request.UpdateCardRequest;
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

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/bulk")
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CardResponse>> getCard(@PathVariable String id) {
        CardResponse response = cardService.getCardById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/study-set")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCardsByStudySet(
            @RequestParam String studySetId) {
        List<CardResponse> response = cardService.getCardsByStudySetId(studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CardResponse>> updateCard(
            @PathVariable String id,
            @Valid @RequestBody UpdateCardRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        CardResponse response = cardService.updateCard(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        cardService.deleteCard(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
