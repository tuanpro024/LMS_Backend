package com.lms.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Client gọi internal API của repo-identity để kiểm tra ticket access.
 */
@Component
public class TicketAccessClient {

    private static final Logger log = LoggerFactory.getLogger(TicketAccessClient.class);

    private final InternalApiClient internalApiClient;
    private final ObjectMapper objectMapper;
    private final String identityServiceName;

    public TicketAccessClient(
            InternalApiClient internalApiClient,
            ObjectMapper objectMapper,
            @Value("${ticket.identity-service-name:repo-identity-api}") String identityServiceName) {
        this.internalApiClient = internalApiClient;
        this.objectMapper = objectMapper;
        this.identityServiceName = identityServiceName;
    }

    /**
     * Kiểm tra xem userId có ticket hợp lệ (OPEN/DONE) cho module không.
     *
     * @param userId ID của user
     * @param module Tên module khớp với TicketModuleEnum (e.g. "FLASHCARD")
     * @return true nếu có quyền CUD
     */
    public boolean checkAccess(String userId, String module) {
        try {
            ResponseEntity<String> response = internalApiClient.send(
                    identityServiceName,
                    "/internal/tickets/check-access",
                    HttpMethod.GET,
                    null,
                    Map.of("userId", userId, "module", module),
                    null,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse: {"success":true,"data":true/false}
                var tree = objectMapper.readTree(response.getBody());
                var dataNode = tree.get("data");
                return dataNode != null && dataNode.asBoolean(false);
            }
        } catch (Exception ex) {
            log.warn("Failed to check ticket access for userId={}, module={}: {}", userId, module, ex.getMessage());
        }
        return false;
    }

    /**
     * Kiểm tra xem userId có bất kỳ ticket hợp lệ nào (bất kỳ module) không.
     * Dùng trong GET endpoints để xác định ticket-holder có được xem DRAFT của mình không.
     *
     * @param userId ID của user
     * @return true nếu user có ít nhất 1 ticket hợp lệ
     */
    public boolean hasAnyTicket(String userId) {
        try {
            ResponseEntity<String> response = internalApiClient.send(
                    identityServiceName,
                    "/internal/tickets/has-any",
                    HttpMethod.GET,
                    null,
                    Map.of("userId", userId),
                    null,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                var tree = objectMapper.readTree(response.getBody());
                var dataNode = tree.get("data");
                return dataNode != null && dataNode.asBoolean(false);
            }
        } catch (Exception ex) {
            log.warn("Failed to check any ticket for userId={}: {}", userId, ex.getMessage());
        }
        return false;
    }
}
