package com.lms.identity.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.common.dto.InteractionLogRequest;
import com.lms.identity.entity.UserInteractionLog;
import com.lms.identity.repository.UserInteractionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service for logging user interactions to both file (via Logback MDC)
 * and database (via JPA). All operations are async to avoid blocking
 * the request thread.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionLogService {

    private static final Logger interactionLogger =
            LoggerFactory.getLogger("com.lms.identity.interaction");

    private final UserInteractionLogRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Logs an interaction event asynchronously.
     * Writes to both structured JSON log file (for ELK) and database (for dashboard).
     *
     * @param request   The interaction event data from frontend
     * @param userId    User ID from SecurityContext (server-side verified)
     * @param userRole  User role from SecurityContext
     * @param ipAddress Client IP address
     * @param userAgent Client User-Agent header
     */
    @Async("interactionLogExecutor")
    public void logInteraction(InteractionLogRequest request,
                                String userId, String userRole,
                                String ipAddress, String userAgent) {
        try {
            // 1. Write to file via MDC + Logback (structured JSON)
            logToFile(request, userId, userRole);

            // 2. Write to database via JPA
            logToDatabase(request, userId, userRole, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("Error logging interaction for user={}, event={}: {}",
                    userId, request.eventType(), e.getMessage(), e);
        }
    }

    private void logToFile(InteractionLogRequest request, String userId, String userRole) {
        try {
            String timestampValue = request.timestamp() != null
                    ? request.timestamp().toString()
                    : Instant.now().toString();

            MDC.put("timestamp", timestampValue);
            MDC.put("sessionId", nullSafe(request.sessionId()));
            MDC.put("userId", nullSafe(userId));
            MDC.put("userRole", nullSafe(userRole));
            MDC.put("eventType", request.eventType() != null ? request.eventType().getValue() : "");
            MDC.put("module", nullSafe(request.module()));
            MDC.put("targetId", nullSafe(request.targetId()));
            MDC.put("pageUrl", nullSafe(request.pageUrl()));

            if (request.metadata() != null && !request.metadata().isEmpty()) {
                try {
                    MDC.put("metadata", objectMapper.writeValueAsString(request.metadata()));
                } catch (JsonProcessingException e) {
                    MDC.put("metadata", request.metadata().toString());
                }
            }

            interactionLogger.info("User interaction logged");
        } finally {
            MDC.clear();
        }
    }

    private void logToDatabase(InteractionLogRequest request,
                                String userId, String userRole,
                                String ipAddress, String userAgent) {
        String metadataStr = null;
        if (request.metadata() != null && !request.metadata().isEmpty()) {
            try {
                metadataStr = objectMapper.writeValueAsString(request.metadata());
            } catch (JsonProcessingException e) {
                metadataStr = request.metadata().toString();
            }
        }

        UserInteractionLog entity = UserInteractionLog.builder()
                .sessionId(request.sessionId())
                .userId(userId)
                .userRole(userRole)
                .eventType(request.eventType())
                .timestamp(request.timestamp() != null ? request.timestamp() : Instant.now())
                .module(request.module())
                .targetId(request.targetId())
                .pageUrl(request.pageUrl())
                .ipAddress(ipAddress)
                .userAgent(truncate(userAgent, 512))
                .metadataJson(metadataStr)
                .build();

        repository.save(entity);
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
