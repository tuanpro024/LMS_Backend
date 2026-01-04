package com.lms.common.jpa;

import jakarta.persistence.Column;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import lombok.Getter;
import lombok.Setter;
import com.lms.common.util.UlidGenerator;

import java.time.Instant;

@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @Column(length = 26, nullable = false, updatable = false, columnDefinition = "nvarchar(26)")
    private String id;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private boolean deleted;

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

    public void onUpdate() {
        updatedAt = Instant.now();
    }
}
