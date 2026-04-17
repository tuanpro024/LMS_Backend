package com.lms.learningpath.controller;

import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.request.UpdatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.TypeName;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private static final String TICKET_MODULE = TicketModuleEnum.LEARNING_PATH.name();

    private final PackageApiDelegate packageDelegate;

    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @RequestBody @Valid CreatePackageRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = packageDelegate.createPackage(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get learning path by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackage(
            @PathVariable String id,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        Set<String> roles = extractRoles(authentication);
        PackageResponse response = packageDelegate.getPackageById(id, userId, roles, TICKET_MODULE);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get all learning paths
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAllPackages(
            @RequestParam(required = false) TypeName type,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        Set<String> roles = extractRoles(authentication);
        List<PackageResponse> response = (type != null)
                ? packageDelegate.getPackagesByType(type, userId, roles, TICKET_MODULE)
                : packageDelegate.getAllPackages(userId, roles, TICKET_MODULE);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Update learning path
     */
    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(
            @PathVariable String id,
            @RequestBody @Valid UpdatePackageRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = packageDelegate.updatePackage(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete learning path
     */
    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        packageDelegate.deletePackage(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Add a Folder to a Package
     */
    @PostMapping("/{packageId}/folders/{folderId}")
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<PackageResponse>> addFolderToPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = packageDelegate.addFolderToPackage(packageId, folderId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Remove a Folder from a Package
     */
    @DeleteMapping("/{packageId}/folders/{folderId}")
    @RequiresTicket(module = TicketModuleEnum.LEARNING_PATH)
    public ResponseEntity<ApiResponse<PackageResponse>> removeFolderFromPackage(
            @PathVariable String packageId,
            @PathVariable String folderId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        PackageResponse response = packageDelegate.removeFolderFromPackage(packageId, folderId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<PackageResponse>> publishPackage(
            @PathVariable String id, Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.publishPackage(id, principal.userId())));
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<PackageResponse>> unpublishPackage(
            @PathVariable String id, Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(packageDelegate.unpublishPackage(id, principal.userId())));
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
