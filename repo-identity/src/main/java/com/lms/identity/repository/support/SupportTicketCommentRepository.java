package com.lms.identity.repository.support;

import com.lms.identity.entity.support.SupportTicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketCommentRepository extends JpaRepository<SupportTicketComment, String> {
    List<SupportTicketComment> findByTicketIdOrderByCreatedAtAsc(String ticketId);
}
