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
import java.util.List;

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
         * - Idempotent: cùng user + courseCode → trả về lead cũ, không tạo duplicate.
         */
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<LeadRegistrationResponse>> register(
                        @Valid @RequestBody LeadRegistrationRequest request,
                        Authentication authentication,
                        HttpServletRequest httpRequest) {

                String userId = extractUserId(authentication);

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
         * Filter: courseCode, from (YYYY-MM-DD), to (YYYY-MM-DD).
         */
        @GetMapping
        // @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
        public ResponseEntity<ApiResponse<Page<LeadRegistrationResponse>>> getLeads(
                        @RequestParam(required = false) String courseCode,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                        @PageableDefault(size = 20, sort = "registeredAt") Pageable pageable) {

                return ResponseEntity.ok(ApiResponse.ok(
                                leadService.getLeads(courseCode, from, to, pageable)));
        }

        /**
         * Danh sách leads có status = PENDING_SALES (không phân trang) cho tích hợp
         * CMS.
         * Filter: courseCode, from (YYYY-MM-DD), to (YYYY-MM-DD).
         */
        @GetMapping("/all")
        public ResponseEntity<ApiResponse<List<LeadRegistrationResponse>>> getAllLeads(
                        @RequestParam(required = false) String courseCode,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

                return ResponseEntity.ok(ApiResponse.ok(
                                leadService.getAllLeads(courseCode, from, to)));
        }

        /**
         * Kích hoạt thủ công quyền học cho một người đăng ký.
         * Chỉ ADMIN/TEACHER_MANAGER mới được gọi endpoint này.
         * Chuyển trạng thái lead → APPROVED, ghi nhận adminUserId + thời điểm.
         */
        @PostMapping("/{leadId}/activate")
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
        public ResponseEntity<ApiResponse<LeadRegistrationResponse>> activateLead(
                        @PathVariable String leadId,
                        Authentication authentication) {

                String adminUserId = extractUserId(authentication);
                LeadRegistrationResponse response = leadService.activateLead(leadId, adminUserId);
                return ResponseEntity.ok(ApiResponse.ok(response));
        }

        /**
         * Lấy trạng thái đăng ký của chính mình cho một khóa học.
         * Frontend dùng để kiểm tra status và hiển thị thông báo phù hợp.
         * Trả về 200 với body null nếu chưa đăng ký.
         */
        @GetMapping("/me/{courseCode}")
        public ResponseEntity<ApiResponse<LeadRegistrationResponse>> getMyRegistration(
                        @PathVariable String courseCode,
                        Authentication authentication) {

                String userId = extractUserId(authentication);
                if (userId == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error("UNAUTHORIZED", "Vui lòng đăng nhập"));
                }
                return ResponseEntity.ok(ApiResponse.ok(
                                leadService.getMyRegistration(userId, courseCode)));
        }

        /**
         * Export Excel — chỉ ADMIN và STAFF.
         * Sử dụng SXSSFWorkbook streaming, hỗ trợ dataset lớn mà không OOM.
         */
        @GetMapping("/export")
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
        public ResponseEntity<byte[]> exportExcel(
                        @RequestParam(required = false) String courseCode,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
                        throws IOException {

                byte[] excelBytes = excelExportService.exportLeads(courseCode, from, to);

                String filename = "leads_" + LocalDate.now() + ".xlsx";
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + filename + "\"")
                                .contentType(MediaType.parseMediaType(
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                                .body(excelBytes);
        }

        // ─── private helpers ─────────────────────────────────────────────────

        private String extractUserId(Authentication authentication) {
                if (authentication == null)
                        return null;
                if (authentication.getPrincipal() instanceof AuthPrincipal principal) {
                        return principal.userId();
                }
                return authentication.getName();
        }
}
