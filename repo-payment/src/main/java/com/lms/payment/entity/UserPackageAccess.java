package com.lms.payment.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.payment.entity.enums.AccessStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_package_access")
public class UserPackageAccess extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String packageId;

    @Column(nullable = false)
    private String packageName;

    @Column(nullable = false)
    private Instant grantedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessStatus status = AccessStatus.ACTIVE;

    public UserPackageAccess() {}

    public UserPackageAccess(String userId, String packageId, String packageName, Instant grantedAt, AccessStatus status) {
        this.userId = userId;
        this.packageId = packageId;
        this.packageName = packageName;
        this.grantedAt = grantedAt;
        this.status = status != null ? status : AccessStatus.ACTIVE;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public Instant getGrantedAt() { return grantedAt; }
    public void setGrantedAt(Instant grantedAt) { this.grantedAt = grantedAt; }
    public AccessStatus getStatus() { return status; }
    public void setStatus(AccessStatus status) { this.status = status; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String userId;
        private String packageId;
        private String packageName;
        private Instant grantedAt;
        private AccessStatus status;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder packageId(String packageId) { this.packageId = packageId; return this; }
        public Builder packageName(String packageName) { this.packageName = packageName; return this; }
        public Builder grantedAt(Instant grantedAt) { this.grantedAt = grantedAt; return this; }
        public Builder status(AccessStatus status) { this.status = status; return this; }
        public UserPackageAccess build() {
            return new UserPackageAccess(userId, packageId, packageName, grantedAt, status);
        }
    }
}
