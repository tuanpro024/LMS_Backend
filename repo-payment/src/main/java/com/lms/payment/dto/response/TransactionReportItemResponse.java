package com.lms.payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class TransactionReportItemResponse {
    private String orderId;
    private Long orderCode;
    private String status;
    private Instant createdAt;
    private Instant paidAt;
    private BigDecimal totalPrice;
    private BuyerInfo buyer;
    private List<CourseInfo> courses;

    public TransactionReportItemResponse() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Long getOrderCode() { return orderCode; }
    public void setOrderCode(Long orderCode) { this.orderCode = orderCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BuyerInfo getBuyer() { return buyer; }
    public void setBuyer(BuyerInfo buyer) { this.buyer = buyer; }
    public List<CourseInfo> getCourses() { return courses; }
    public void setCourses(List<CourseInfo> courses) { this.courses = courses; }

    public static class BuyerInfo {
        private String userId;
        private String username;
        private String fullName;
        private String email;

        public BuyerInfo() {}
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class CourseInfo {
        private String packageId;
        private String packageName;
        private BigDecimal price;
        private String thumbnail;
        private String itemType;
        private Integer durationInDays;

        public CourseInfo() {}
        public String getPackageId() { return packageId; }
        public void setPackageId(String packageId) { this.packageId = packageId; }
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getThumbnail() { return thumbnail; }
        public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
        public String getItemType() { return itemType; }
        public void setItemType(String itemType) { this.itemType = itemType; }
        public Integer getDurationInDays() { return durationInDays; }
        public void setDurationInDays(Integer durationInDays) { this.durationInDays = durationInDays; }
    }
}
