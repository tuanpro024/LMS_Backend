package com.lms.payment.service;

import com.lms.payment.dto.request.AddToCartRequest;
import com.lms.payment.dto.response.AccessCheckResponse;
import com.lms.payment.dto.response.CartItemResponse;
import com.lms.payment.dto.response.CheckoutResponse;
import com.lms.payment.dto.response.OrderResponse;

import java.util.List;

public interface PaymentService {
    List<CartItemResponse> getCart(String userId);
    CartItemResponse addToCart(String userId, AddToCartRequest request);
    void removeFromCart(String userId, String packageId);

    /**
     * Checkout: tạo Order từ giỏ hàng + tạo PayOS payment link.
     * Trả về CheckoutResponse chứa checkoutUrl để redirect user.
     */
    CheckoutResponse checkout(String userId);

    /**
     * Xử lý webhook từ PayOS khi thanh toán thành công/thất bại.
     */
    void handlePayOsWebhook(Object requestBody);

    /**
     * Lấy trạng thái order (cho frontend polling sau khi user quay về).
     */
    OrderResponse getOrderStatus(String orderId);

    AccessCheckResponse checkAccess(String userId, String packageId);
    void enrollFree(String userId, String packageId, String packageName, String thumbnail);
    List<OrderResponse> getMyOrders(String userId);
    List<String> getMyOwnedPackageIds(String userId);
}
