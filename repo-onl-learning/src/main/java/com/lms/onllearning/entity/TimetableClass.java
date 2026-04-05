package com.lms.onllearning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "timetable_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableClass {

    @Id
    @Column(length = 40, nullable = false)
    private String id;

    @Column(name = "class_code", length = 100)
    private String classCode;

    @Column(name = "class_name", length = 255)
    private String className;

    @Column(name = "class_status", length = 50)
    private String classStatus;

    @Column(name = "start_date", length = 40)
    private String startDate;

    @Column(name = "end_date", length = 40)
    private String endDate;

    @Column(name = "sync_version", nullable = false)
    private Long syncVersion;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
