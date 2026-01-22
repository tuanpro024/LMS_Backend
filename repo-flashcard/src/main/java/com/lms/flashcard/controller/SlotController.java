package com.lms.flashcard.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.SlotApiDelegate;
import com.lms.content.common.dto.request.CreateSlotRequest;
import com.lms.content.common.dto.request.UpdateSlotRequest;
import com.lms.content.common.dto.response.SlotResponse;
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

    private final SlotApiDelegate delegate;

    @PostMapping
    public ResponseEntity<ApiResponse<SlotResponse>> createSlot(
            @RequestBody @Valid CreateSlotRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SlotResponse response = delegate.createSlot(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SlotResponse>> getSlotById(@PathVariable String id) {
        SlotResponse response = delegate.getSlotById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getAllSlots() {
        List<SlotResponse> response = delegate.getAllSlots();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getSlotsBySubjectId(
            @PathVariable String subjectId) {
        List<SlotResponse> response = delegate.getSlotsBySubjectId(subjectId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SlotResponse>> updateSlot(
            @PathVariable String id,
            @RequestBody @Valid UpdateSlotRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SlotResponse response = delegate.updateSlot(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        delegate.deleteSlot(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{slotId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<SlotResponse>> addFolderToSlot(
            @PathVariable String slotId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SlotResponse response = delegate.addFolderToSlot(slotId, folderId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{slotId}/folders/{folderId}")
    public ResponseEntity<ApiResponse<SlotResponse>> removeFolderFromSlot(
            @PathVariable String slotId,
            @PathVariable String folderId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SlotResponse response = delegate.removeFolderFromSlot(slotId, folderId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
