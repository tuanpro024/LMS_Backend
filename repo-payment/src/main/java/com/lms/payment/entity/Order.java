package com.lms.payment.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.payment.entity.enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(unique = true)
    private Long orderCode;

    private String paymentLinkId;

    private Instant paidAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(String userId, BigDecimal totalPrice, OrderStatus status, Long orderCode, String paymentLinkId, Instant paidAt, List<OrderItem> items) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.orderCode = orderCode;
        this.paymentLinkId = paymentLinkId;
        this.paidAt = paidAt;
        if (items != null) {
            this.items = items;
            for (OrderItem item : items) {
                item.setOrder(this);
            }
        }
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Long getOrderCode() { return orderCode; }
    public void setOrderCode(Long orderCode) { this.orderCode = orderCode; }
    public String getPaymentLinkId() { return paymentLinkId; }
    public void setPaymentLinkId(String paymentLinkId) { this.paymentLinkId = paymentLinkId; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public List<OrderItem> getItems() { return items; }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String userId;
        private BigDecimal totalPrice;
        private OrderStatus status;
        private Long orderCode;
        private String paymentLinkId;
        private Instant paidAt;
        private List<OrderItem> items = new ArrayList<>();

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder totalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; return this; }
        public Builder status(OrderStatus status) { this.status = status; return this; }
        public Builder orderCode(Long orderCode) { this.orderCode = orderCode; return this; }
        public Builder paymentLinkId(String paymentLinkId) { this.paymentLinkId = paymentLinkId; return this; }
        public Builder paidAt(Instant paidAt) { this.paidAt = paidAt; return this; }
        public Builder items(List<OrderItem> items) { this.items = items; return this; }
        public Order build() {
            return new Order(userId, totalPrice, status, orderCode, paymentLinkId, paidAt, items);
        }
    }
}
