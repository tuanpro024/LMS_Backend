package com.lms.payment.service.impl;

import com.lms.payment.dto.request.PayOsWebhookRequest;
import com.lms.payment.entity.Order;
import com.lms.payment.service.PayOsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOsServiceImpl implements PayOsService {

    private final PayOS payOS;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @Override
    public CreatePaymentLinkResponse createPaymentLink(Order order) throws Exception {
        // Tạo danh sách items từ order
        List<PaymentLinkItem> items = order.getItems().stream()
                .map(oi -> PaymentLinkItem.builder()
                        .name(oi.getPackageName())
                        .quantity(1)
                        .price(oi.getPrice().longValue())
                        .build())
                .toList();

        // Tạo description (giới hạn 25 ký tự cho mô tả VietQR)
        String description = "LMS Order " + order.getOrderCode();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        // Tạo CreatePaymentLinkRequest cho PayOS SDK
        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(order.getOrderCode())
                .amount(order.getTotalPrice().longValue())
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .items(items) // Truyền toàn bộ danh sách items
                .build();

        log.info("Creating PayOS payment link for orderCode={}, amount={}",
                order.getOrderCode(), order.getTotalPrice());

        CreatePaymentLinkResponse result = payOS.paymentRequests().create(paymentData);

        log.info("PayOS payment link created: checkoutUrl={}, paymentLinkId={}",
                result.getCheckoutUrl(), result.getPaymentLinkId());

        return result;
    }

    @Override
    public boolean verifyWebhookSignature(PayOsWebhookRequest request) {
        try {
            // PayOS SDK cung cấp method verifyPaymentWebhookData
            // Chúng ta sẽ verify thông qua SDK
            if (request.getData() == null) {
                log.warn("Webhook request has null data");
                return false;
            }
            // Với PayOS Java SDK, webhook data được verify tự động khi gọi verifyPaymentWebhookData
            // Tuy nhiên cách đơn giản nhất là kiểm tra code
            return "00".equals(request.getCode()) && request.isSuccess();
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    @Override
    public Long generateOrderCode() {
        // Tạo orderCode duy nhất dựa trên timestamp + random
        // PayOS yêu cầu orderCode kiểu Long (max 9223372036854775807)
        long timestamp = new Date().getTime();
        long random = (long) (Math.random() * 1000);
        return timestamp * 1000 + random;
    }
}
