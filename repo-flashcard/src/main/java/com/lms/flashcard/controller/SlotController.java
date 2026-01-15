package com.lms.flashcard.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.flashcard.dto.request.CreateSlotRequest;
import com.lms.flashcard.dto.request.UpdateSlotRequest;
import com.lms.flashcard.dto.response.SlotResponse;
import com.lms.flashcard.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @PostMapping
    public ResponseEntity<ApiResponse<SlotResponse>> createSlot(
            @RequestBody @Valid CreateSlotRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SlotResponse response = slotService.createSlot(request, principal.userId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SlotResponse>> getSlotById(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SlotResponse response = slotService.getSlotById(id, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getSlots(
            @RequestParam(required = false) String subjectId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        List<SlotResponse> response;

        if (subjectId != null) {
            response = slotService.getSlotsBySubjectId(subjectId, principal.userId());
        } else {
            response = slotService.getAllSlots(principal.userId());
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SlotResponse>> updateSlot(
            @PathVariable String id,
            @RequestBody @Valid UpdateSlotRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SlotResponse response = slotService.updateSlot(id, request, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        slotService.deleteSlot(id, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{slotId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<SlotResponse>> addFolderToSlot(
            @PathVariable String slotId,
            @PathVariable String folderId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SlotResponse response = slotService.addFolderToSlot(slotId, folderId, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{slotId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<SlotResponse>> removeFolderFromSlot(
            @PathVariable String slotId,
            @PathVariable String folderId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SlotResponse response = slotService.removeFolderFromSlot(slotId, folderId, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
