package com.lms.identity.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "external_teachers", indexes = {
        @Index(name = "idx_external_teacher_email", columnList = "email")
})
public class ExternalTeacher extends BaseEntity {

    @Column(name = "cms_user_id", unique = true)
    private String cmsUserId;

    @Column(nullable = false)
    private String email;

    private String fullName;

    private String phoneNumber;

    private String address;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "cms_status")
    private String cmsStatus;

    private String qualification;

    @Column(columnDefinition = "TEXT")
    private String teachingStyle;

    private String videoIntroLink;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String fullDescription;

    private Double rating;

    private Integer totalClasses;

    private Integer totalStudents;

    private Integer totalSessions;
}
