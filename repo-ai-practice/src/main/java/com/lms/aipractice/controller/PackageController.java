package com.lms.aipractice.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.dto.response.TypeResponse;
import com.lms.content.common.entity.TypeName;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private static final String TICKET_MODULE = TicketModuleEnum.AI.name();

    private final PackageApiDelegate delegate;

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<TypeResponse>>> getPackageTypes() {
        return ResponseEntity.ok(ApiResponse.ok(delegate.getPackageTypes()));
    }

    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @RequestBody @Valid CreatePackageRequest request, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(delegate.createPackage(request, p.userId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> getById(
            @PathVariable String id,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        Set<String> roles = extractRoles(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                delegate.getPackageById(id, userId, roles, TICKET_MODULE)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAll(
            @RequestParam(required = false) TypeName type,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        Set<String> roles = extractRoles(authentication);
        List<PackageResponse> resp = type != null
                ? delegate.getPackagesByType(type, userId, roles, TICKET_MODULE)
                : delegate.getAllPackages(userId, roles, TICKET_MODULE);
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }

    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<PackageResponse>> update(
            @PathVariable String id,
            @RequestBody @Valid UpdatePackageRequest request,
            Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.updatePackage(id, request, p.userId())));
    }

    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        delegate.deletePackage(id, p.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{packageId}/folders/{folderId}")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<PackageResponse>> addFolder(
            @PathVariable String packageId, @PathVariable String folderId, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.addFolderToPackage(packageId, folderId, p.userId())));
    }

    @DeleteMapping("/{packageId}/folders/{folderId}")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<PackageResponse>> removeFolder(
            @PathVariable String packageId, @PathVariable String folderId, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.removeFolderFromPackage(packageId, folderId, p.userId())));
    }

    @PostMapping("/{id}/publish")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<PackageResponse>> publishPackage(
            @PathVariable String id, Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.publishPackage(id, principal.userId())));
    }

    @PostMapping("/{id}/unpublish")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<PackageResponse>> unpublishPackage(
            @PathVariable String id, Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.unpublishPackage(id, principal.userId())));
    }

    private String extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return null;
        if (authentication.getPrincipal() instanceof AuthPrincipal p)
            return p.userId();
        return null;
    }

    private Set<String> extractRoles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return Collections.emptySet();
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());
    }
}
