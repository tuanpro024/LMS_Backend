package com.lms.onllearning.entity;

import com.lms.onllearning.entity.enums.TimetableParticipantRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "timetable_class_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableClassParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", length = 40, nullable = false)
    private String classId;

    @Column(length = 255, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_role", nullable = false, length = 20)
    private TimetableParticipantRole participantRole;

    @Column(name = "sync_version", nullable = false)
    private Long syncVersion;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
