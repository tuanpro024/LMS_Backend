package com.lms.payment.dto.response;

import java.math.BigDecimal;

public class OrderItemResponse {
    private String id;
    private String packageId;
    private String packageName;
    private BigDecimal price;
    private String thumbnail;

    public OrderItemResponse() {}

    public OrderItemResponse(String id, String packageId, String packageName, BigDecimal price, String thumbnail) {
        this.id = id;
        this.packageId = packageId;
        this.packageName = packageName;
        this.price = price;
        this.thumbnail = thumbnail;
    }

    public String getId() { return id; }
    public String getPackageId() { return packageId; }
    public String getPackageName() { return packageName; }
    public BigDecimal getPrice() { return price; }
    public String getThumbnail() { return thumbnail; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String packageId;
        private String packageName;
        private BigDecimal price;
        private String thumbnail;

        public Builder id(String id) { this.id = id; return this; }
        public Builder packageId(String packageId) { this.packageId = packageId; return this; }
        public Builder packageName(String packageName) { this.packageName = packageName; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder thumbnail(String thumbnail) { this.thumbnail = thumbnail; return this; }
        public OrderItemResponse build() {
            return new OrderItemResponse(id, packageId, packageName, price, thumbnail);
        }
    }
}
