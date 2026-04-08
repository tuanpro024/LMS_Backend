package com.lms.identity.repository;

import com.lms.identity.entity.ticket.Ticket;
import com.lms.identity.entity.ticket.TicketModule;
import com.lms.identity.entity.ticket.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {

    /**
     * Tìm tất cả ticket theo người được assign (phân trang).
     */
    Page<Ticket> findByAssignedId(String assignedId, Pageable pageable);

    /**
     * Tìm tất cả ticket (phân trang) — dành cho ADMIN/MANAGER.
     */
    Page<Ticket> findAll(Pageable pageable);

    /**
     * Kiểm tra access: user có ticket ứng với module và status không phải CLOSE.
     */
    boolean existsByAssignedIdAndModuleAndStatusNot(String assignedId, TicketModule module, TicketStatus status);

    /**
     * Tìm ticket theo người assign và module.
     */
    List<Ticket> findByAssignedIdAndModule(String assignedId, TicketModule module);
}
