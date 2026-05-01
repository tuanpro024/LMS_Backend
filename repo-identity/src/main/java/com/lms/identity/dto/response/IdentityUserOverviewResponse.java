package com.lms.identity.dto.response;

public class IdentityUserOverviewResponse {
    private long totalUsers;
    private long totalTeachers;
    private long totalManagers;
    private long totalStudents;

    public IdentityUserOverviewResponse() {
    }

    public IdentityUserOverviewResponse(long totalUsers, long totalTeachers, long totalManagers, long totalStudents) {
        this.totalUsers = totalUsers;
        this.totalTeachers = totalTeachers;
        this.totalManagers = totalManagers;
        this.totalStudents = totalStudents;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalTeachers() {
        return totalTeachers;
    }

    public void setTotalTeachers(long totalTeachers) {
        this.totalTeachers = totalTeachers;
    }

    public long getTotalManagers() {
        return totalManagers;
    }

    public void setTotalManagers(long totalManagers) {
        this.totalManagers = totalManagers;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }
}