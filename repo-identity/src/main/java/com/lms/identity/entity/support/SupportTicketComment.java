package com.lms.identity.entity.support;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "support_ticket_comments", indexes = {
        @Index(name = "idx_stc_ticket_id", columnList = "ticket_id")
})
public class SupportTicketComment extends BaseEntity {

    @Column(name = "ticket_id", nullable = false, length = 26)
    private String ticketId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_by", nullable = false, length = 26)
    private String createdBy;
}
