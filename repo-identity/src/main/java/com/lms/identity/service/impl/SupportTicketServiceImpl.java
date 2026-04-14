package com.lms.identity.service.impl;

import com.lms.identity.entity.support.SupportTicketCategory;
import com.lms.identity.entity.support.SupportTicketPriority;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.notification.NotificationEvent;
import com.lms.common.notification.NotificationPublisher;
import com.lms.common.notification.ResourceType;
import com.lms.common.security.AuthPrincipal;
import com.lms.identity.dto.request.support.SupportTicketCommentRequest;
import com.lms.identity.dto.request.support.SupportTicketCreateRequest;
import com.lms.identity.dto.response.support.SupportTicketCommentResponse;
import com.lms.identity.dto.response.support.SupportTicketResponse;
import com.lms.identity.entity.User;
import com.lms.identity.entity.support.SupportTicket;
import com.lms.identity.entity.support.SupportTicketComment;
import com.lms.identity.entity.support.SupportTicketStatus;
import com.lms.identity.repository.UserRepository;
import com.lms.identity.repository.support.SupportTicketCommentRepository;
import com.lms.identity.repository.support.SupportTicketRepository;
import com.lms.identity.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketCommentRepository supportTicketCommentRepository;
    private final UserRepository userRepository;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public SupportTicketResponse createTicket(AuthPrincipal principal, SupportTicketCreateRequest request) {
        SupportTicket ticket = SupportTicket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .files(request.getFiles())
                .status(SupportTicketStatus.OPEN)
                .createdBy(principal.userId())
                .build();
        
        SupportTicket saved = supportTicketRepository.save(ticket);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> listMyTickets(AuthPrincipal principal, String status, String category, String priority, String sortBy, String sortDirection, int page, int size) {
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        Specification<SupportTicket> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // Fixed filter: only tickets created by this user
            predicates.add(cb.equal(root.get("createdBy"), principal.userId()));
            
            // Optional filter: status
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), SupportTicketStatus.valueOf(status)));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status
                }
            }
            
            // Optional filter: category
            if (category != null && !category.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("category"), SupportTicketCategory.valueOf(category)));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid category
                }
            }
            
            // Optional filter: priority
            if (priority != null && !priority.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("priority"), SupportTicketPriority.valueOf(priority)));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid priority
                }
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<SupportTicket> result = supportTicketRepository.findAll(spec, pageable);
        return result.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicket(AuthPrincipal principal, String id) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Support ticket not found"));
        
        if (!ticket.getCreatedBy().equals(principal.userId())) {
            // Check if user is admin/manager
            if (!principal.hasAnyRole(List.of("ROLE_ADMIN", "ROLE_TEACHER_MANAGER"))) {
                throw new ApiException(ErrorCode.FORBIDDEN, "No authority access this ticket");
            }
        }
        
        return toResponse(ticket);
    }

    @Override
    @Transactional
    public SupportTicketResponse cancelTicket(AuthPrincipal principal, String id) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Support ticket not found"));
        
        if (!ticket.getCreatedBy().equals(principal.userId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Only creator can cancel");
        }
        
        if (ticket.getStatus() == SupportTicketStatus.COMPLETED || ticket.getStatus() == SupportTicketStatus.CANCELLED) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot cancel this ticket");
        }
        
        ticket.setStatus(SupportTicketStatus.CANCELLED);
        return toResponse(supportTicketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> listManagementTickets(AuthPrincipal principal, String search, String status, String category, String priority, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Specification<SupportTicket> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
            }
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), SupportTicketStatus.valueOf(status)));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status
                }
            }
            if (category != null && !category.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("category"), SupportTicketCategory.valueOf(category)));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }
            if (priority != null && !priority.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("priority"), SupportTicketPriority.valueOf(priority)));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        Page<SupportTicket> result = supportTicketRepository.findAll(spec, pageable);
        return result.map(this::toResponse);
    }

    @Override
    @Transactional
    public SupportTicketCommentResponse replyTicket(AuthPrincipal principal, String id, SupportTicketCommentRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Support ticket not found"));
        
        SupportTicketComment comment = SupportTicketComment.builder()
                .ticketId(id)
                .content(request.getContent())
                .createdBy(principal.userId())
                .build();
        
        SupportTicketComment saved = supportTicketCommentRepository.save(comment);
        
        // Auto-resolve ticket
        ticket.setStatus(SupportTicketStatus.COMPLETED);
        supportTicketRepository.save(ticket);
        
        // Notify student
        publishTicketResolvedNotification(ticket);
        
        return toCommentResponse(saved);
    }

    private void publishTicketResolvedNotification(SupportTicket ticket) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ticketId", ticket.getId());
        payload.put("title", ticket.getTitle());
        
        NotificationEvent event = new NotificationEvent(
                ticket.getCreatedBy(),
                "support.ticket.resolved",
                "Yêu cầu hỗ trợ đã được xử lý",
                String.format("Yêu cầu '%s' của bạn đã được quản trị viên phản hồi và đóng lại.", ticket.getTitle()),
                ResourceType.SUPPORT_TICKET,
                ticket.getId(),
                payload,
                "st-resolved-" + ticket.getId()
        );
        
        notificationPublisher.publish(event);
        log.info("Published support.ticket.resolved notification for ticket={} to user={}", 
                ticket.getId(), ticket.getCreatedBy());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketCommentResponse> listComments(AuthPrincipal principal, String ticketId) {
        // Simple security check
        getTicket(principal, ticketId);
        
        return supportTicketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    private SupportTicketResponse toResponse(SupportTicket ticket) {
        User creator = userRepository.findById(ticket.getCreatedBy()).orElse(null);
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .files(ticket.getFiles())
                .createdBy(ticket.getCreatedBy())
                .creatorName(creator != null ? creator.getFullName() : "Người dùng")
                .creatorAvatarUrl(creator != null ? creator.getAvatarUrl() : null)
                .assignedTo(ticket.getAssignedTo())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private SupportTicketCommentResponse toCommentResponse(SupportTicketComment comment) {
        User creator = userRepository.findById(comment.getCreatedBy()).orElse(null);
        return SupportTicketCommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicketId())
                .content(comment.getContent())
                .createdBy(comment.getCreatedBy())
                .creatorName(creator != null ? creator.getFullName() : "Quản trị viên")
                .creatorAvatarUrl(creator != null ? creator.getAvatarUrl() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
