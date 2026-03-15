package com.lms.payment.entity;

import com.lms.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(nullable = false)
    private String packageId;

    @Column(nullable = false)
    private String packageName;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;
    
    private String thumbnail;

    public OrderItem() {}

    public OrderItem(Order order, String packageId, String packageName, BigDecimal price, String thumbnail) {
        this.order = order;
        this.packageId = packageId;
        this.packageName = packageName;
        this.price = price;
        this.thumbnail = thumbnail;
    }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Order order;
        private String packageId;
        private String packageName;
        private BigDecimal price;
        private String thumbnail;

        public Builder order(Order order) { this.order = order; return this; }
        public Builder packageId(String packageId) { this.packageId = packageId; return this; }
        public Builder packageName(String packageName) { this.packageName = packageName; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder thumbnail(String thumbnail) { this.thumbnail = thumbnail; return this; }
        public OrderItem build() {
            return new OrderItem(order, packageId, packageName, price, thumbnail);
        }
    }
}
