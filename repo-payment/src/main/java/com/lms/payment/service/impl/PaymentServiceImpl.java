package com.lms.payment.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.payment.dto.request.AddToCartRequest;
import com.lms.payment.dto.response.AccessCheckResponse;
import com.lms.payment.dto.response.CartItemResponse;
import com.lms.payment.dto.response.OrderItemResponse;
import com.lms.payment.dto.response.OrderResponse;
import com.lms.payment.entity.CartItem;
import com.lms.payment.entity.Order;
import com.lms.payment.entity.OrderItem;
import com.lms.payment.entity.UserPackageAccess;
import com.lms.payment.entity.enums.AccessStatus;
import com.lms.payment.entity.enums.OrderStatus;
import com.lms.payment.repository.CartItemRepository;
import com.lms.payment.repository.OrderRepository;
import com.lms.payment.repository.UserPackageAccessRepository;
import com.lms.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final CartItemRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserPackageAccessRepository accessRepository;

    @Override
    public List<CartItemResponse> getCart(String userId) {
        return cartRepository.findByUserId(userId).stream()
                .map(this::mapToCartResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartItemResponse addToCart(String userId, AddToCartRequest request) {
        // Check if already in cart
        if (cartRepository.findByUserIdAndPackageId(userId, request.getPackageId()).isPresent()) {
            throw new ApiException(ErrorCode.E240, "Package already in cart");
        }
        
        // Check if already owned
        if (accessRepository.findByUserIdAndPackageIdAndStatus(userId, request.getPackageId(), AccessStatus.ACTIVE).isPresent()) {
            throw new ApiException(ErrorCode.E240, "Package already owned");
        }

        CartItem item = CartItem.builder()
                .userId(userId)
                .packageId(request.getPackageId())
                .packageName(request.getPackageName())
                .price(request.getPrice())
                .thumbnail(request.getThumbnail())
                .build();
        
        return mapToCartResponse(cartRepository.save(item));
    }

    @Override
    @Transactional
    public void removeFromCart(String userId, String packageId) {
        cartRepository.findByUserIdAndPackageId(userId, packageId)
                .ifPresent(cartRepository::delete);
    }

    @Override
    @Transactional
    public OrderResponse checkout(String userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new ApiException(ErrorCode.E240, "Cart is empty");
        }

        BigDecimal total = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .userId(userId)
                .totalPrice(total)
                .status(OrderStatus.PENDING)
                .build();

        for (CartItem ci : cartItems) {
            order.addItem(OrderItem.builder()
                    .packageId(ci.getPackageId())
                    .packageName(ci.getPackageName())
                    .price(ci.getPrice())
                    .thumbnail(ci.getThumbnail())
                    .build());
        }

        Order saved = orderRepository.save(order);
        
        // Clear cart
        cartRepository.deleteByUserId(userId);

        return mapToOrderResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse payOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ApiException(ErrorCode.E240, "Order is not pending");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setPaidAt(Instant.now());

        // Grant access
        for (OrderItem item : order.getItems()) {
            grantAccess(order.getUserId(), item.getPackageId(), item.getPackageName());
        }

        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    public AccessCheckResponse checkAccess(String userId, String packageId) {
        boolean hasAccess = accessRepository.findByUserIdAndPackageIdAndStatus(userId, packageId, AccessStatus.ACTIVE).isPresent();
        return AccessCheckResponse.builder()
                .userId(userId)
                .packageId(packageId)
                .hasAccess(hasAccess)
                .build();
    }

    @Override
    @Transactional
    public void enrollFree(String userId, String packageId, String packageName, String thumbnail) {
        if (accessRepository.findByUserIdAndPackageIdAndStatus(userId, packageId, AccessStatus.ACTIVE).isPresent()) {
            return; // Already has access
        }
        grantAccess(userId, packageId, packageName);
    }

    @Override
    public List<OrderResponse> getMyOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getMyOwnedPackageIds(String userId) {
        return accessRepository.findByUserIdAndStatus(userId, AccessStatus.ACTIVE).stream()
                .map(UserPackageAccess::getPackageId)
                .collect(Collectors.toList());
    }

    private void grantAccess(String userId, String packageId, String packageName) {
        UserPackageAccess access = UserPackageAccess.builder()
                .userId(userId)
                .packageId(packageId)
                .packageName(packageName)
                .grantedAt(Instant.now())
                .status(AccessStatus.ACTIVE)
                .build();
        accessRepository.save(access);
    }

    private CartItemResponse mapToCartResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .packageId(item.getPackageId())
                .packageName(item.getPackageName())
                .price(item.getPrice())
                .thumbnail(item.getThumbnail())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .items(order.getItems().stream().map(item -> 
                    OrderItemResponse.builder()
                        .id(item.getId())
                        .packageId(item.getPackageId())
                        .packageName(item.getPackageName())
                        .price(item.getPrice())
                        .thumbnail(item.getThumbnail())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
