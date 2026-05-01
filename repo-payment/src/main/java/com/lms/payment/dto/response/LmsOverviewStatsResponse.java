package com.lms.payment.dto.response;

import java.math.BigDecimal;

public class LmsOverviewStatsResponse {
    private long totalVideoCourses;
    private long totalOnlineCourses;
    private BigDecimal totalVideoCourseRevenue;
    private BigDecimal totalMembershipRevenue;

    public LmsOverviewStatsResponse() {
    }

    public LmsOverviewStatsResponse(long totalVideoCourses,
                                    long totalOnlineCourses,
                                    BigDecimal totalVideoCourseRevenue,
                                    BigDecimal totalMembershipRevenue) {
        this.totalVideoCourses = totalVideoCourses;
        this.totalOnlineCourses = totalOnlineCourses;
        this.totalVideoCourseRevenue = totalVideoCourseRevenue;
        this.totalMembershipRevenue = totalMembershipRevenue;
    }

    public long getTotalVideoCourses() {
        return totalVideoCourses;
    }

    public void setTotalVideoCourses(long totalVideoCourses) {
        this.totalVideoCourses = totalVideoCourses;
    }

    public long getTotalOnlineCourses() {
        return totalOnlineCourses;
    }

    public void setTotalOnlineCourses(long totalOnlineCourses) {
        this.totalOnlineCourses = totalOnlineCourses;
    }

    public BigDecimal getTotalVideoCourseRevenue() {
        return totalVideoCourseRevenue;
    }

    public void setTotalVideoCourseRevenue(BigDecimal totalVideoCourseRevenue) {
        this.totalVideoCourseRevenue = totalVideoCourseRevenue;
    }

    public BigDecimal getTotalMembershipRevenue() {
        return totalMembershipRevenue;
    }

    public void setTotalMembershipRevenue(BigDecimal totalMembershipRevenue) {
        this.totalMembershipRevenue = totalMembershipRevenue;
    }
}