package com.lms.payment.dto.response;

public class AccessCheckResponse {
    private String userId;
    private String packageId;
    private boolean hasAccess;

    public AccessCheckResponse() {}

    public AccessCheckResponse(String userId, String packageId, boolean hasAccess) {
        this.userId = userId;
        this.packageId = packageId;
        this.hasAccess = hasAccess;
    }

    public String getUserId() { return userId; }
    public String getPackageId() { return packageId; }
    public boolean isHasAccess() { return hasAccess; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String userId;
        private String packageId;
        private boolean hasAccess;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder packageId(String packageId) { this.packageId = packageId; return this; }
        public Builder hasAccess(boolean hasAccess) { this.hasAccess = hasAccess; return this; }
        public AccessCheckResponse build() {
            return new AccessCheckResponse(userId, packageId, hasAccess);
        }
    }
}
