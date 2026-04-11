package com.lms.identity.entity.support;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "support_tickets", indexes = {
        @Index(name = "idx_support_ticket_created_by", columnList = "created_by"),
        @Index(name = "idx_support_ticket_status", columnList = "status")
})
public class SupportTicket extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SupportTicketStatus status = SupportTicketStatus.OPEN;

    @Column(name = "created_by", nullable = false, length = 26)
    private String createdBy;

    @Column(name = "assigned_to", length = 26)
    private String assignedTo;
}
