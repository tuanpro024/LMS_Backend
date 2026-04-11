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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageApiDelegate delegate;

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<TypeResponse>>> getPackageTypes() {
        return ResponseEntity.ok(ApiResponse.ok(delegate.getPackageTypes()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @RequestBody @Valid CreatePackageRequest request, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(delegate.createPackage(request, p.userId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(delegate.getPackageById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAll(
            @RequestParam(required = false) TypeName type) {
        List<PackageResponse> resp = type != null
                ? delegate.getPackagesByType(type)
                : delegate.getAllPackages();
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<PackageResponse>> update(
            @PathVariable String id,
            @RequestBody @Valid UpdatePackageRequest request,
            Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.updatePackage(id, request, p.userId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        delegate.deletePackage(id, p.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{packageId}/folders/{folderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<PackageResponse>> addFolder(
            @PathVariable String packageId, @PathVariable String folderId, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.addFolderToPackage(packageId, folderId, p.userId())));
    }

    @DeleteMapping("/{packageId}/folders/{folderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.AI)
    public ResponseEntity<ApiResponse<PackageResponse>> removeFolder(
            @PathVariable String packageId, @PathVariable String folderId, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(delegate.removeFolderFromPackage(packageId, folderId, p.userId())));
    }
}
