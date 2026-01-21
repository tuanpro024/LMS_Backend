package com.lms.flashcard.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.flashcard.dto.request.UpdateCardStatusRequest;
import com.lms.flashcard.dto.response.CardResponse;
import com.lms.flashcard.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateCardStatus(
            @PathVariable String id,
            @RequestBody @Valid UpdateCardStatusRequest request) {

        cardService.updateCardStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
