package com.lms.payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {
    private String id;
    private String userId;
    private BigDecimal totalPrice;
    private String status;
    private Long orderCode;
    private String paymentLinkId;
    private Instant createdAt;
    private Instant paidAt;
    private List<OrderItemResponse> items;

    public OrderResponse() {}

    public OrderResponse(String id, String userId, BigDecimal totalPrice, String status, Long orderCode, String paymentLinkId, Instant createdAt, Instant paidAt, List<OrderItemResponse> items) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderCode = orderCode;
        this.paymentLinkId = paymentLinkId;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.items = items;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public Long getOrderCode() { return orderCode; }
    public String getPaymentLinkId() { return paymentLinkId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public List<OrderItemResponse> getItems() { return items; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String userId;
        private BigDecimal totalPrice;
        private String status;
        private Long orderCode;
        private String paymentLinkId;
        private Instant createdAt;
        private Instant paidAt;
        private List<OrderItemResponse> items;

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder totalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder orderCode(Long orderCode) { this.orderCode = orderCode; return this; }
        public Builder paymentLinkId(String paymentLinkId) { this.paymentLinkId = paymentLinkId; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder paidAt(Instant paidAt) { this.paidAt = paidAt; return this; }
        public Builder items(List<OrderItemResponse> items) { this.items = items; return this; }
        public OrderResponse build() {
            return new OrderResponse(id, userId, totalPrice, status, orderCode, paymentLinkId, createdAt, paidAt, items);
        }
    }
}
