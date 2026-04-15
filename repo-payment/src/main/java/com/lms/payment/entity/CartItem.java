package com.lms.payment.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.payment.entity.enums.ItemType;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type")
    private ItemType itemType = ItemType.COURSE;

    @Column(name = "duration_in_days")
    private Integer durationInDays; // Only for MEMBERSHIP type

    public CartItem() {}

    public CartItem(String userId, String packageId, String packageName, BigDecimal price, String thumbnail, ItemType itemType, Integer durationInDays) {
        this.userId = userId;
        this.packageId = packageId;
        this.packageName = packageName;
        this.price = price;
        this.thumbnail = thumbnail;
        this.itemType = itemType != null ? itemType : ItemType.COURSE;
        this.durationInDays = durationInDays;
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
    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }
    public Integer getDurationInDays() { return durationInDays; }
    public void setDurationInDays(Integer durationInDays) { this.durationInDays = durationInDays; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String userId;
        private String packageId;
        private String packageName;
        private BigDecimal price;
        private String thumbnail;
        private ItemType itemType;
        private Integer durationInDays;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder packageId(String packageId) { this.packageId = packageId; return this; }
        public Builder packageName(String packageName) { this.packageName = packageName; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder thumbnail(String thumbnail) { this.thumbnail = thumbnail; return this; }
        public Builder itemType(ItemType itemType) { this.itemType = itemType; return this; }
        public Builder durationInDays(Integer durationInDays) { this.durationInDays = durationInDays; return this; }
        public CartItem build() {
            return new CartItem(userId, packageId, packageName, price, thumbnail, itemType, durationInDays);
        }
    }
}
