package com.lms.payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class CartItemResponse {
    private String id;
    private String packageId;
    private String packageName;
    private BigDecimal price;
    private String thumbnail;
    private String itemType;
    private Integer durationInDays;
    private Instant createdAt;

    public CartItemResponse() {}

    public CartItemResponse(String id, String packageId, String packageName, BigDecimal price, String thumbnail, String itemType, Integer durationInDays, Instant createdAt) {
        this.id = id;
        this.packageId = packageId;
        this.packageName = packageName;
        this.price = price;
        this.thumbnail = thumbnail;
        this.itemType = itemType;
        this.durationInDays = durationInDays;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getPackageId() { return packageId; }
    public String getPackageName() { return packageName; }
    public BigDecimal getPrice() { return price; }
    public String getThumbnail() { return thumbnail; }
    public String itemType() { return itemType; }
    public Integer getDurationInDays() { return durationInDays; }
    public Instant getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String packageId;
        private String packageName;
        private BigDecimal price;
        private String thumbnail;
        private String itemType;
        private Integer durationInDays;
        private Instant createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder packageId(String packageId) { this.packageId = packageId; return this; }
        public Builder packageName(String packageName) { this.packageName = packageName; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder thumbnail(String thumbnail) { this.thumbnail = thumbnail; return this; }
        public Builder itemType(String itemType) { this.itemType = itemType; return this; }
        public Builder durationInDays(Integer durationInDays) { this.durationInDays = durationInDays; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public CartItemResponse build() {
            return new CartItemResponse(id, packageId, packageName, price, thumbnail, itemType, durationInDays, createdAt);
        }
    }
}
