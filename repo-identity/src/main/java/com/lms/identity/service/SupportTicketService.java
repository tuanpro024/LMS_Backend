package com.lms.identity.service;

import com.lms.common.security.AuthPrincipal;
import com.lms.identity.dto.request.support.SupportTicketCommentRequest;
import com.lms.identity.dto.request.support.SupportTicketCreateRequest;
import com.lms.identity.dto.response.support.SupportTicketCommentResponse;
import com.lms.identity.dto.response.support.SupportTicketResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SupportTicketService {
    SupportTicketResponse createTicket(AuthPrincipal principal, SupportTicketCreateRequest request);
    Page<SupportTicketResponse> listMyTickets(AuthPrincipal principal, int page, int size);
    SupportTicketResponse getTicket(AuthPrincipal principal, String id);
    SupportTicketResponse cancelTicket(AuthPrincipal principal, String id);
    
    // Management
    Page<SupportTicketResponse> listManagementTickets(AuthPrincipal principal, String search, String status, int page, int size);
    SupportTicketCommentResponse replyTicket(AuthPrincipal principal, String id, SupportTicketCommentRequest request);
    
    // Comments
    List<SupportTicketCommentResponse> listComments(AuthPrincipal principal, String ticketId);
}
