package com.lms.identity.entity.ticket;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_assigned_module", columnList = "assigned_id, module"),
        @Index(name = "idx_ticket_created_by", columnList = "created_by"),
        @Index(name = "idx_ticket_status", columnList = "status")
})
public class Ticket extends BaseEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketModule module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    /**
     * FK → users.id — người được giao việc (TEACHER hoặc COLLABORATOR)
     */
    @Column(name = "assigned_id", nullable = false, length = 26)
    private String assignedId;

    /**
     * FK → users.id — người tạo ticket (ADMIN hoặc TEACHER_MANAGER)
     */
    @Column(name = "created_by", nullable = false, length = 26)
    private String createdBy;
}
