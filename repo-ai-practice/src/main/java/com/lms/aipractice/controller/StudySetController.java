package com.lms.aipractice.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.content.common.dto.request.CreateStudySetRequest;
import com.lms.content.common.dto.request.UpdateStudySetRequest;
import com.lms.content.common.dto.response.StudySetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/study-sets")
@RequiredArgsConstructor
public class StudySetController {

    private final StudySetApiDelegate delegate;

    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<StudySetResponse>> create(
            @RequestBody @Valid CreateStudySetRequest request, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(delegate.createStudySet(request, p.userId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySetResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(delegate.getStudySetById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getAll(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String q) {
        if (userId != null) return ResponseEntity.ok(ApiResponse.ok(delegate.getStudySetsByUserId(userId)));
        if (q != null) return ResponseEntity.ok(ApiResponse.ok(delegate.searchStudySets(q)));
        return ResponseEntity.ok(ApiResponse.ok(delegate.getAllStudySets()));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getByFolder(@PathVariable String folderId) {
        return ResponseEntity.ok(ApiResponse.ok(delegate.getStudySetsByFolderId(folderId)));
    }

    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<StudySetResponse>> update(
            @PathVariable String id,
            @RequestBody @Valid UpdateStudySetRequest request,
            Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.updateStudySet(id, request, p.userId())));
    }

    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        delegate.deleteStudySet(id, p.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
