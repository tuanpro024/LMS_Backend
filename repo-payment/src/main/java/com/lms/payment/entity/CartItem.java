package com.lms.payment.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String packageId;

    @Column(nullable = false)
    private String packageName;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    private String thumbnail;

    public CartItem() {}

    public CartItem(String userId, String packageId, String packageName, BigDecimal price, String thumbnail) {
        this.userId = userId;
        this.packageId = packageId;
        this.packageName = packageName;
        this.price = price;
        this.thumbnail = thumbnail;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
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
        private String userId;
        private String packageId;
        private String packageName;
        private BigDecimal price;
        private String thumbnail;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder packageId(String packageId) { this.packageId = packageId; return this; }
        public Builder packageName(String packageName) { this.packageName = packageName; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder thumbnail(String thumbnail) { this.thumbnail = thumbnail; return this; }
        public CartItem build() {
            return new CartItem(userId, packageId, packageName, price, thumbnail);
        }
    }
}
