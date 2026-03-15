package com.lms.payment.service;

import com.lms.payment.dto.request.AddToCartRequest;
import com.lms.payment.dto.response.AccessCheckResponse;
import com.lms.payment.dto.response.CartItemResponse;
import com.lms.payment.dto.response.OrderResponse;

import java.util.List;

public interface PaymentService {
    List<CartItemResponse> getCart(String userId);
    CartItemResponse addToCart(String userId, AddToCartRequest request);
    void removeFromCart(String userId, String packageId);
    OrderResponse checkout(String userId);
    OrderResponse payOrder(String orderId);
    AccessCheckResponse checkAccess(String userId, String packageId);
    void enrollFree(String userId, String packageId, String packageName, String thumbnail);
    List<OrderResponse> getMyOrders(String userId);
    List<String> getMyOwnedPackageIds(String userId);
}
