package com.lms.payment.service;

import com.lms.payment.dto.request.PayOsWebhookRequest;
import com.lms.payment.entity.Order;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

public interface PayOsService {
    /**
     * Tạo payment link trên PayOS cho order.
     * @return CreatePaymentLinkResponse chứa checkoutUrl, qrCode, paymentLinkId, ...
     */
    CreatePaymentLinkResponse createPaymentLink(Order order) throws Exception;

    /**
     * Xác thực chữ ký webhook từ PayOS.
     * @return true nếu signature hợp lệ
     */
    boolean verifyWebhookSignature(PayOsWebhookRequest request);

    /**
     * Tạo mã orderCode duy nhất (kiểu Long) cho PayOS.
     */
    Long generateOrderCode();
}
