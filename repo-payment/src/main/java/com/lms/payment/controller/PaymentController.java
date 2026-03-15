package com.lms.payment.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.payment.dto.request.AddToCartRequest;
import com.lms.payment.dto.response.AccessCheckResponse;
import com.lms.payment.dto.response.CartItemResponse;
import com.lms.payment.dto.response.OrderResponse;
import com.lms.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ========== CART ENDPOINTS ==========

    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCart(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getCart(principal.userId())));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            Authentication authentication,
            @RequestBody AddToCartRequest request) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(paymentService.addToCart(principal.userId(), request)));
    }

    @DeleteMapping("/cart/{packageId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            Authentication authentication,
            @PathVariable String packageId) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        paymentService.removeFromCart(principal.userId(), packageId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ========== ORDER & CHECKOUT ENDPOINTS ==========

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(paymentService.checkout(principal.userId())));
    }

    @PostMapping("/orders/{orderId}/pay")
    public ResponseEntity<ApiResponse<OrderResponse>> payOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.payOrder(orderId)));
    }

    @GetMapping("/orders/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getMyOrders(principal.userId())));
    }

    // ========== ACCESS ENDPOINTS ==========

    @GetMapping("/access-check")
    public ResponseEntity<ApiResponse<AccessCheckResponse>> checkAccess(
            Authentication authentication,
            @RequestParam String packageId) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(paymentService.checkAccess(principal.userId(), packageId)));
    }

    @GetMapping("/access/my-ids")
    public ResponseEntity<ApiResponse<List<String>>> getMyOwnedPackageIds(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getMyOwnedPackageIds(principal.userId())));
    }

    @PostMapping("/enroll-free")
    public ResponseEntity<ApiResponse<Void>> enrollFree(
            Authentication authentication,
            @RequestParam String packageId,
            @RequestParam String packageName,
            @RequestParam(required = false) String thumbnail) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        paymentService.enrollFree(principal.userId(), packageId, packageName, thumbnail);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
