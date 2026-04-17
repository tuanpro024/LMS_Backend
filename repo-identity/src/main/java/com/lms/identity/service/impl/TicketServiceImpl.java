package com.lms.identity.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.notification.NotificationEvent;
import com.lms.common.notification.NotificationPublisher;
import com.lms.common.notification.ResourceType;
import com.lms.common.security.AuthPrincipal;
import com.lms.identity.dto.request.ticket.CreateTicketRequest;
import com.lms.identity.dto.request.ticket.UpdateTicketRequest;
import com.lms.identity.dto.response.ticket.TicketAssigneeOptionResponse;
import com.lms.identity.dto.response.ticket.TicketResponse;
import com.lms.identity.entity.RoleName;
import com.lms.identity.entity.User;
import com.lms.identity.entity.UserStatus;
import com.lms.identity.entity.ticket.Ticket;
import com.lms.identity.entity.ticket.TicketModule;
import com.lms.identity.entity.ticket.TicketStatus;
import com.lms.identity.repository.TicketRepository;
import com.lms.identity.repository.UserRepository;
import com.lms.identity.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final Set<String> PRIVILEGED_ROLES = Set.of(
            RoleName.ROLE_ADMIN.name(), RoleName.ROLE_TEACHER_MANAGER.name());

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final NotificationPublisher notificationPublisher;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TicketResponse createTicket(AuthPrincipal principal, CreateTicketRequest request) {
        requirePrivileged(principal);

        User assignedUser = userRepository.findById(request.getAssignedId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                        "Assigned user not found: " + request.getAssignedId()));

        Ticket ticket = Ticket.builder()
                .description(request.getDescription())
                .module(request.getModule())
                .status(TicketStatus.OPEN)
                .assignedId(request.getAssignedId())
                .createdBy(principal.userId())
                .build();

        Ticket saved = ticketRepository.save(ticket);

        // Gửi notification cho người được assign
        publishTicketAssignedNotification(saved, assignedUser, principal);

        User creator = userRepository.findById(principal.userId()).orElse(null);
        return toResponse(saved, assignedUser, creator);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicket(String ticketId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        User assigned = userRepository.findById(ticket.getAssignedId()).orElse(null);
        User creator = userRepository.findById(ticket.getCreatedBy()).orElse(null);
        return toResponse(ticket, assigned, creator);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> listTickets(AuthPrincipal principal, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Ticket> ticketPage;
        if (isPrivileged(principal)) {
            // ADMIN / TEACHER_MANAGER xem tất cả
            ticketPage = ticketRepository.findAll(pageable);
        } else {
            // Các role khác chỉ xem ticket của mình
            ticketPage = ticketRepository.findByAssignedId(principal.userId(), pageable);
        }

        return ticketPage.map(t -> {
            User assigned = userRepository.findById(t.getAssignedId()).orElse(null);
            User creator = userRepository.findById(t.getCreatedBy()).orElse(null);
            return toResponse(t, assigned, creator);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TicketResponse updateTicket(String ticketId, AuthPrincipal principal, UpdateTicketRequest request) {
        requirePrivileged(principal);

        Ticket ticket = findTicketOrThrow(ticketId);

        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getModule() != null) {
            ticket.setModule(request.getModule());
        }
        if (request.getAssignedId() != null) {
            userRepository.findById(request.getAssignedId())
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                            "Assigned user not found: " + request.getAssignedId()));
            ticket.setAssignedId(request.getAssignedId());
        }

        Ticket saved = ticketRepository.save(ticket);
        User assigned = userRepository.findById(saved.getAssignedId()).orElse(null);
        User creator = userRepository.findById(saved.getCreatedBy()).orElse(null);
        return toResponse(saved, assigned, creator);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteTicket(String ticketId, AuthPrincipal principal) {
        requirePrivileged(principal);
        Ticket ticket = findTicketOrThrow(ticketId);
        ticketRepository.delete(ticket);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MARK DONE (người được assign)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TicketResponse markDone(String ticketId, AuthPrincipal principal) {
        Ticket ticket = findTicketOrThrow(ticketId);

        // Chỉ người được assign mới được mark DONE
        if (!ticket.getAssignedId().equals(principal.userId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only the assigned user can mark ticket as DONE");
        }

        if (ticket.getStatus() == TicketStatus.CLOSE) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot mark a CLOSE ticket as DONE");
        }

        ticket.setStatus(TicketStatus.DONE);
        Ticket saved = ticketRepository.save(ticket);

        User assigned = userRepository.findById(saved.getAssignedId()).orElse(null);
        User creator = userRepository.findById(saved.getCreatedBy()).orElse(null);
        return toResponse(saved, assigned, creator);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLOSE (ADMIN/MANAGER)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TicketResponse closeTicket(String ticketId, AuthPrincipal principal) {
        requirePrivileged(principal);

        Ticket ticket = findTicketOrThrow(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSE) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Ticket is already CLOSE");
        }

        ticket.setStatus(TicketStatus.CLOSE);
        Ticket saved = ticketRepository.save(ticket);

        // Gửi notification cho người được assign
        User assignedUser = userRepository.findById(saved.getAssignedId()).orElse(null);
        publishTicketClosedNotification(saved, assignedUser, principal);

        User creator = userRepository.findById(saved.getCreatedBy()).orElse(null);
        return toResponse(saved, assignedUser, creator);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHECK ACCESS (dùng bởi internal API)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean checkAccess(String userId, TicketModule module) {
        // Có ticket ứng với module và status != CLOSE (tức là OPEN hoặc DONE)
        return ticketRepository.existsByAssignedIdAndModuleAndStatusNot(userId, module, TicketStatus.CLOSE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyTicket(String userId) {
        // Có ít nhất 1 ticket hợp lệ (bất kỳ module, status != CLOSE)
        return ticketRepository.existsByAssignedIdAndStatusNot(userId, TicketStatus.CLOSE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketAssigneeOptionResponse> listAssignableUsers(String query) {
        List<User> teachers = userRepository.findByRolesName(RoleName.ROLE_TEACHER);
        List<User> collaborators = userRepository.findByRolesName(RoleName.ROLE_COLLABORATOR);

        Map<String, User> uniqueUsers = new LinkedHashMap<>();
        teachers.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .forEach(u -> uniqueUsers.put(u.getId(), u));
        collaborators.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .forEach(u -> uniqueUsers.put(u.getId(), u));

        String normalizedQuery = query == null ? null : query.trim().toLowerCase(Locale.ROOT);

        return uniqueUsers.values().stream()
                .filter(u -> {
                    if (normalizedQuery == null || normalizedQuery.isBlank()) {
                        return true;
                    }
                    String email = u.getEmail() == null ? "" : u.getEmail().toLowerCase(Locale.ROOT);
                    String fullName = u.getFullName() == null ? "" : u.getFullName().toLowerCase(Locale.ROOT);
                    return email.contains(normalizedQuery) || fullName.contains(normalizedQuery);
                })
                .sorted((u1, u2) -> {
                    String n1 = u1.getFullName() == null ? "" : u1.getFullName();
                    String n2 = u2.getFullName() == null ? "" : u2.getFullName();
                    return n1.compareToIgnoreCase(n2);
                })
                .map(u -> TicketAssigneeOptionResponse.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .roles(u.getRoles().stream()
                                .map(r -> r.getName().name())
                                .collect(Collectors.toSet()))
                        .build())
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private Ticket findTicketOrThrow(String ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Ticket not found: " + ticketId));
    }

    private boolean isPrivileged(AuthPrincipal principal) {
        return principal.roles() != null && principal.roles().stream().anyMatch(PRIVILEGED_ROLES::contains);
    }

    private void requirePrivileged(AuthPrincipal principal) {
        if (!isPrivileged(principal)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only ADMIN or TEACHER_MANAGER can perform this action");
        }
    }

    private TicketResponse toResponse(Ticket ticket, User assigned, User creator) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .description(ticket.getDescription())
                .module(ticket.getModule())
                .status(ticket.getStatus())
                .assignedId(ticket.getAssignedId())
                .assignedEmail(assigned != null ? assigned.getEmail() : null)
                .assignedFullName(assigned != null ? assigned.getFullName() : null)
                .createdBy(ticket.getCreatedBy())
                .createdByEmail(creator != null ? creator.getEmail() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private void publishTicketAssignedNotification(Ticket ticket, User assignedUser, AuthPrincipal principal) {
        if (assignedUser == null)
            return;
        NotificationEvent event = new NotificationEvent(
                assignedUser.getId(),
                "ticket.assigned",
                "Bạn được giao công việc mới",
                String.format("Bạn được giao ticket [%s] cho module %s. Vui lòng xem chi tiết.",
                        ticket.getId(), ticket.getModule()),
                ResourceType.TICKET,
                ticket.getId(),
                Map.of("module", ticket.getModule().name(), "status", ticket.getStatus().name()),
                "ticket-assigned-" + ticket.getId());
        notificationPublisher.publish(event);
        log.info("Published ticket.assigned notification for ticket={} to user={}", ticket.getId(),
                assignedUser.getId());
    }

    private void publishTicketClosedNotification(Ticket ticket, User assignedUser, AuthPrincipal principal) {
        if (assignedUser == null)
            return;
        NotificationEvent event = new NotificationEvent(
                assignedUser.getId(),
                "ticket.closed",
                "Ticket của bạn đã được đóng",
                String.format("Ticket [%s] module %s đã được đóng bởi quản lý.",
                        ticket.getId(), ticket.getModule()),
                ResourceType.TICKET,
                ticket.getId(),
                Map.of("module", ticket.getModule().name(), "status", ticket.getStatus().name()),
                "ticket-closed-" + ticket.getId());
        notificationPublisher.publish(event);
        log.info("Published ticket.closed notification for ticket={} to user={}", ticket.getId(), assignedUser.getId());
    }
}
