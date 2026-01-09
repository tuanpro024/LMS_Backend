package com.lms.common.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import com.lms.common.util.UlidGenerator;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(length = 26, nullable = false, updatable = false, columnDefinition = "nvarchar(26)")
    private String id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private boolean deleted;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UlidGenerator.nextUlid();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        deleted = false;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}
