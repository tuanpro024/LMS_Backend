package com.lms.payment.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.payment.dto.request.AddToCartRequest;
import com.lms.payment.dto.response.*;
import com.lms.payment.entity.CartItem;
import com.lms.payment.entity.Order;
import com.lms.payment.entity.OrderItem;
import com.lms.payment.entity.UserPackageAccess;
import com.lms.payment.entity.enums.AccessStatus;
import com.lms.payment.entity.enums.OrderStatus;
import com.lms.payment.repository.CartItemRepository;
import com.lms.payment.repository.OrderRepository;
import com.lms.payment.repository.UserPackageAccessRepository;
import com.lms.payment.service.PayOsService;
import com.lms.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final CartItemRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserPackageAccessRepository accessRepository;
    private final PayOsService payOsService;
    private final PayOS payOS;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final com.lms.payment.repository.MembershipPlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
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

        java.math.BigDecimal price = request.getPrice();
        Integer duration = null;

        if (request.getItemType() == com.lms.payment.entity.enums.ItemType.MEMBERSHIP) {
            com.lms.payment.entity.MembershipPlan plan = planRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Membership plan not found"));
            price = plan.getPrice();
            duration = plan.getDurationInDays();
        }

        CartItem item = CartItem.builder()
                .userId(userId)
                .packageId(request.getPackageId())
                .packageName(request.getPackageName())
                .price(price)
                .thumbnail(request.getThumbnail())
                .itemType(request.getItemType())
                .durationInDays(duration) // Cần thêm field này vào CartItem
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
    public CheckoutResponse checkout(String userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new ApiException(ErrorCode.E240, "Cart is empty");
        }

        BigDecimal total = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tạo orderCode duy nhất cho PayOS
        Long orderCode = payOsService.generateOrderCode();

        Order order = Order.builder()
                .userId(userId)
                .totalPrice(total)
                .status(OrderStatus.PENDING)
                .orderCode(orderCode)
                .build();

        for (CartItem ci : cartItems) {
            order.addItem(OrderItem.builder()
                    .packageId(ci.getPackageId())
                    .packageName(ci.getPackageName())
                    .price(ci.getPrice())
                    .thumbnail(ci.getThumbnail())
                    .itemType(ci.getItemType())
                    .durationInDays(ci.getDurationInDays()) // Cần thêm field vào OrderItem
                    .build());
        }

        Order saved = orderRepository.save(order);

        // Tạo PayOS payment link
        try {
            CreatePaymentLinkResponse payosResponse = payOsService.createPaymentLink(saved);

            // Cập nhật order với thông tin từ PayOS
            saved.setPaymentLinkId(payosResponse.getPaymentLinkId());
            saved.setStatus(OrderStatus.PAYING);
            orderRepository.save(saved);

            // Xóa cart sau khi tạo order thành công
            cartRepository.deleteByUserId(userId);

            return CheckoutResponse.builder()
                    .orderId(saved.getId())
                    .checkoutUrl(payosResponse.getCheckoutUrl())
                    .qrCode(payosResponse.getQrCode())
                    .build();

        } catch (Exception e) {
            log.error("Failed to create PayOS payment link for order {}", saved.getId(), e);
            // Nếu tạo payment link thất bại → hủy order
            saved.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(saved);
            throw new ApiException(ErrorCode.E240, "Failed to create payment link: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handlePayOsWebhook(Object requestBody) {
        try {
            WebhookData data = payOS.webhooks().verify(requestBody);
            log.info("Verified PayOS webhook data: code={}, orderCode={}", data.getCode(), data.getOrderCode());

            Long orderCode = data.getOrderCode();
            
            if (orderCode == null) {
                log.warn("Webhook data has null orderCode, ignoring");
                return;
            }

            Order order = orderRepository.findByOrderCode(orderCode)
                    .orElse(null);

            if (order == null) {
                log.warn("Order not found for orderCode={}", orderCode);
                return;
            }

            // Nếu đã xử lý rồi thì bỏ qua (idempotent)
            if (order.getStatus() == OrderStatus.COMPLETED) {
                log.info("Order {} already completed, skipping", order.getId());
                return;
            }

            // Kiểm tra thanh toán thành công (Mã code 00 = Thành công)
            if ("00".equals(data.getCode())) {
                log.info("Payment successful for order {}, granting access", order.getId());
                order.setStatus(OrderStatus.COMPLETED);
                order.setPaidAt(Instant.now());

                // Cấp quyền truy cập cho từng package
                for (OrderItem item : order.getItems()) {
                    if (item.getItemType() == com.lms.payment.entity.enums.ItemType.MEMBERSHIP) {
                        // Phát sự kiện mua premium (Identity và Notification sẽ lắng nghe)
                        com.lms.common.event.MembershipPurchasedEvent event = com.lms.common.event.MembershipPurchasedEvent.builder()
                                .userId(order.getUserId())
                                .orderId(order.getId())
                                .packageName(item.getPackageName())
                                .durationInDays(item.getDurationInDays() != null ? item.getDurationInDays() : 30)
                                .build();
                        try {
                            String json = objectMapper.writeValueAsString(event);
                            kafkaTemplate.send("membership.purchased", json);
                        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                            log.error("Failed to serialize MembershipPurchasedEvent for order {}", order.getId(), e);
                        }
                    } else {
                        grantAccess(order.getUserId(), item.getPackageId(), item.getPackageName());
                    }
                }

                orderRepository.save(order);
                eventPublisher.publishEvent(new com.lms.payment.event.PaymentCompletedInternalEvent(this, order));
            } else {
                log.warn("Payment failed or different status for order {}: code={}, desc={}",
                        order.getId(), data.getCode(), data.getDesc());
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        } catch (Exception e) {
            log.error("Failed to verify or process PayOS webhook", e);
            throw new RuntimeException("Webhook verification failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderStatus(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Order not found"));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
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
        
        // Publish event for notification
        eventPublisher.publishEvent(new com.lms.payment.event.FreeEnrollmentCompletedInternalEvent(
                this, userId, packageId, packageName, thumbnail));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMyOwnedPackageIds(String userId) {
        return accessRepository.findByUserIdAndStatus(userId, AccessStatus.ACTIVE).stream()
                .map(UserPackageAccess::getPackageId)
                .collect(Collectors.toList());
    }

    private void grantAccess(String userId, String packageId, String packageName) {
        // Tránh grantAccess trùng
        if (accessRepository.findByUserIdAndPackageIdAndStatus(userId, packageId, AccessStatus.ACTIVE).isPresent()) {
            log.info("User {} already has access to package {}, skipping", userId, packageId);
            return;
        }
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
                .itemType(item.getItemType() != null ? item.getItemType().name() : null)
                .durationInDays(item.getDurationInDays())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        boolean hasMembership = order.getItems().stream()
                .anyMatch(item -> item.getItemType() == com.lms.payment.entity.enums.ItemType.MEMBERSHIP);
        String label = hasMembership ? "Gói thành viên" : "Khóa học";

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .orderCode(order.getOrderCode())
                .paymentLinkId(order.getPaymentLinkId())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .label(label)
                .items(order.getItems().stream().map(item -> 
                    OrderItemResponse.builder()
                        .id(item.getId())
                        .packageId(item.getPackageId())
                        .packageName(item.getPackageName())
                        .price(item.getPrice())
                        .thumbnail(item.getThumbnail())
                        .itemType(item.getItemType() != null ? item.getItemType().name() : null)
                        .durationInDays(item.getDurationInDays())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
