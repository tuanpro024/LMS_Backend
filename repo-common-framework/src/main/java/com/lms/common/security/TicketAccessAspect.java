package com.lms.common.security;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.http.TicketAccessClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * AOP Aspect kiểm tra ticket access cho các method được đánh dấu @RequiresTicket.
 *
 * <p>Logic:
 * <ol>
 *   <li>Nếu user có role ADMIN hoặc TEACHER_MANAGER → bypass.</li>
 *   <li>Ngược lại gọi identity-service để kiểm tra có ticket hợp lệ không.</li>
 *   <li>Không có → throw 403 FORBIDDEN.</li>
 * </ol>
 * </p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TicketAccessAspect {

    private static final Set<String> PRIVILEGED_ROLES = Set.of(
            "ROLE_ADMIN", "ROLE_TEACHER_MANAGER");

    private final TicketAccessClient ticketAccessClient;

    @Before("@annotation(requiresTicket)")
    public void checkTicketAccess(RequiresTicket requiresTicket) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Not authenticated");
        }

        // Lấy roles
        Set<String> userRoles = authentication.getAuthorities().stream()
                .map(ga -> ga.getAuthority())
                .collect(java.util.stream.Collectors.toSet());

        // ADMIN và TEACHER_MANAGER được bypass
        log.info("TicketAccessAspect: userRoles found: {}", userRoles);
        if (userRoles.stream().anyMatch(role -> PRIVILEGED_ROLES.contains(role.toUpperCase()))) {
            log.info("TicketAccessAspect: privileged role bypass triggered for module={}", requiresTicket.module());
            return;
        }

        // Lấy userId từ principal
        if (!(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid principal");
        }

        String userId = principal.userId();
        String module = requiresTicket.module().name();

        boolean hasAccess = ticketAccessClient.checkAccess(userId, module);
        if (!hasAccess) {
            log.warn("TicketAccessAspect: DENIED userId={} module={}", userId, module);
            throw new ApiException(ErrorCode.FORBIDDEN,
                    "Bạn không có ticket hợp lệ cho module " + module + ". Vui lòng liên hệ quản lý để được cấp quyền.");
        }

        log.debug("TicketAccessAspect: ALLOWED userId={} module={}", userId, module);
    }
}
