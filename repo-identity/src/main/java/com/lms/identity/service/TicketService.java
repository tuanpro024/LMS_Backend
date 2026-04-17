package com.lms.identity.service;

import com.lms.common.security.AuthPrincipal;
import com.lms.identity.dto.request.ticket.CreateTicketRequest;
import com.lms.identity.dto.request.ticket.UpdateTicketRequest;
import com.lms.identity.dto.response.ticket.TicketAssigneeOptionResponse;
import com.lms.identity.dto.response.ticket.TicketResponse;
import com.lms.identity.entity.ticket.TicketModule;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TicketService {

    /**
     * Tạo ticket mới — chỉ ADMIN/TEACHER_MANAGER.
     */
    TicketResponse createTicket(AuthPrincipal principal, CreateTicketRequest request);

    /**
     * Lấy chi tiết ticket.
     */
    TicketResponse getTicket(String ticketId);

    /**
     * Danh sách ticket (ADMIN/MANAGER xem tất cả; TEACHER/COLLABORATOR xem của
     * mình).
     */
    Page<TicketResponse> listTickets(AuthPrincipal principal, int page, int size);

    /**
     * Cập nhật ticket — chỉ ADMIN/TEACHER_MANAGER.
     */
    TicketResponse updateTicket(String ticketId, AuthPrincipal principal, UpdateTicketRequest request);

    /**
     * Xóa ticket — chỉ ADMIN/TEACHER_MANAGER.
     */
    void deleteTicket(String ticketId, AuthPrincipal principal);

    /**
     * Người được assign đánh dấu DONE.
     */
    TicketResponse markDone(String ticketId, AuthPrincipal principal);

    /**
     * ADMIN/MANAGER CLOSE ticket; gửi notification cho người assigned.
     */
    TicketResponse closeTicket(String ticketId, AuthPrincipal principal);

    /**
     * Kiểm tra xem userId có ticket hợp lệ (OPEN hoặc DONE) cho module không.
     * Dùng bởi internal API.
     */
    boolean checkAccess(String userId, TicketModule module);

    /**
     * Kiểm tra xem userId có bất kỳ ticket hợp lệ nào (bất kỳ module) không.
     * Dùng bởi /internal/tickets/has-any để xác định ticket-holder cho việc hiển thị DRAFT.
     */
    boolean hasAnyTicket(String userId);

    /**
     * Danh sách user có thể assign ticket (TEACHER/COLLABORATOR) cho UI chọn
     * assignee.
     */
    List<TicketAssigneeOptionResponse> listAssignableUsers(String query);
}
