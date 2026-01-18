package com.lms.writing.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.writing.dto.request.CreateSlotRequest;
import com.lms.writing.dto.request.UpdateSlotRequest;
import com.lms.writing.dto.response.SlotResponse;
import com.lms.writing.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
@RequiredArgsConstructor
@Slf4j
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
    public ResponseEntity<ApiResponse<SlotResponse>> getSlot(@PathVariable String id) {
        SlotResponse response = slotService.getSlotById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getSlots(
            @RequestParam(required = false) String subjectId) {

        List<SlotResponse> response;

        if (subjectId != null) {
            response = slotService.getSlotsBySubjectId(subjectId);
        } else {
            response = slotService.getAllSlots();
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
