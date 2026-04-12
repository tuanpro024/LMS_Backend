package com.lms.identity.repository.support;

import com.lms.identity.entity.support.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, String>, JpaSpecificationExecutor<SupportTicket> {
    Page<SupportTicket> findByCreatedBy(String createdBy, Pageable pageable);
}
