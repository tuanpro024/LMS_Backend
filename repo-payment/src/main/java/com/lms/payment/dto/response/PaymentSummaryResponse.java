package com.lms.payment.dto.response;

import java.math.BigDecimal;

public class PaymentSummaryResponse {
    private BigDecimal totalRevenue;
    private long totalTransactions;
    private long totalStudents;
    private long totalCoursesSold;
    private long totalMembershipsSold;

    public PaymentSummaryResponse() {}

    public PaymentSummaryResponse(BigDecimal totalRevenue, long totalTransactions, long totalStudents, long totalCoursesSold, long totalMembershipsSold) {
        this.totalRevenue = totalRevenue;
        this.totalTransactions = totalTransactions;
        this.totalStudents = totalStudents;
        this.totalCoursesSold = totalCoursesSold;
        this.totalMembershipsSold = totalMembershipsSold;
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public long getTotalCoursesSold() { return totalCoursesSold; }
    public void setTotalCoursesSold(long totalCoursesSold) { this.totalCoursesSold = totalCoursesSold; }
    public long getTotalMembershipsSold() { return totalMembershipsSold; }
    public void setTotalMembershipsSold(long totalMembershipsSold) { this.totalMembershipsSold = totalMembershipsSold; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private BigDecimal totalRevenue;
        private long totalTransactions;
        private long totalStudents;
        private long totalCoursesSold;
        private long totalMembershipsSold;

        public Builder totalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; return this; }
        public Builder totalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; return this; }
        public Builder totalStudents(long totalStudents) { this.totalStudents = totalStudents; return this; }
        public Builder totalCoursesSold(long totalCoursesSold) { this.totalCoursesSold = totalCoursesSold; return this; }
        public Builder totalMembershipsSold(long totalMembershipsSold) { this.totalMembershipsSold = totalMembershipsSold; return this; }
        public PaymentSummaryResponse build() {
            return new PaymentSummaryResponse(totalRevenue, totalTransactions, totalStudents, totalCoursesSold, totalMembershipsSold);
        }
    }
}
