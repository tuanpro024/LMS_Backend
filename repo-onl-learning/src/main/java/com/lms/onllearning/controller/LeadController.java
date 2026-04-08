package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.onllearning.dto.request.LeadRegistrationRequest;
import com.lms.onllearning.dto.response.LeadRegistrationResponse;
import com.lms.onllearning.service.ExcelExportService;
import com.lms.onllearning.service.ILeadService;
import com.lms.onllearning.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

    private final ILeadService leadService;
    private final ExcelExportService excelExportService;
    private final RateLimitService rateLimitService;

    /**
     * Đăng ký tư vấn khóa học.
     * - userId lấy từ JWT (authentication.getName()), không nhận từ client.
     * - Rate-limited: 5 req/phút/user + 10 req/phút/IP.
     * - Idempotent: cùng user + syllabus → trả về lead cũ, không tạo duplicate.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LeadRegistrationResponse>> register(
            @Valid @RequestBody LeadRegistrationRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

                String userId = authentication != null ? authentication.getName() : null;
                if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
                        userId = principal.userId();
                }

        // Rate-limit check: cả IP và userId
        if (!rateLimitService.tryConsume(httpRequest, userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "60")
                    .body(ApiResponse.error("RATE_LIMIT_EXCEEDED",
                            "Quá nhiều yêu cầu, vui lòng thử lại sau 1 phút"));
        }

        LeadRegistrationResponse response = leadService.register(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    /**
     * Danh sách leads — chỉ ADMIN và STAFF.
     * Filter: syllabusId, from (YYYY-MM-DD), to (YYYY-MM-DD).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<LeadRegistrationResponse>>> getLeads(
            @RequestParam(required = false) String syllabusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "registeredAt") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(
                leadService.getLeads(syllabusId, from, to, pageable)));
    }

    /**
     * Export Excel — chỉ ADMIN và STAFF.
     * Sử dụng SXSSFWorkbook streaming, hỗ trợ dataset lớn mà không OOM.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String syllabusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
            throws IOException {

        byte[] excelBytes = excelExportService.exportLeads(syllabusId, from, to);

        String filename = "leads_" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
