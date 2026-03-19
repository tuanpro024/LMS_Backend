package com.lms.payment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.lms.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payos")
@RequiredArgsConstructor
@Slf4j
public class PayOsWebhookController {

    private final PaymentService paymentService;

    /**
     * Webhook endpoint nhận thông tin thanh toán từ PayOS.
     * Endpoint này KHÔNG yêu cầu JWT authentication.
     * PayOS sẽ gọi POST tới endpoint này khi user thanh toán xong.
     * Cần trả về HTTP 2xx để PayOS biết đã nhận thành công.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody Object requestBody) {
        log.info("PayOS webhook received");

        try {
            paymentService.handlePayOsWebhook(requestBody);
        } catch (Exception e) {
            log.error("Error processing PayOS webhook", e);
            // Vẫn trả 200 để PayOS không retry liên tục
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}
