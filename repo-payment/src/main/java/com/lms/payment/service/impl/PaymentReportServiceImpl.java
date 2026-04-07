package com.lms.payment.service.impl;

import com.lms.payment.dto.response.PaymentSummaryResponse;
import com.lms.payment.dto.response.TransactionReportItemResponse;
import com.lms.payment.entity.Order;
import com.lms.payment.entity.enums.OrderStatus;
import com.lms.payment.repository.OrderRepository;
import com.lms.payment.service.PaymentReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PaymentReportServiceImpl implements PaymentReportService {

    private final OrderRepository orderRepository;

    public PaymentReportServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public PaymentSummaryResponse getSummary() {
        BigDecimal totalRevenue = orderRepository.sumTotalPriceByStatus(OrderStatus.COMPLETED);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        long totalTransactions = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long totalStudents = orderRepository.countDistinctUserIdByStatus(OrderStatus.COMPLETED);
        long totalCoursesSold = orderRepository.countTotalItemsByStatus(OrderStatus.COMPLETED);

        return PaymentSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTransactions(totalTransactions)
                .totalStudents(totalStudents)
                .totalCoursesSold(totalCoursesSold)
                .build();
    }

    @Override
    public Page<TransactionReportItemResponse> getTransactionReport(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::mapToTransactionReportItem);
    }

    private TransactionReportItemResponse mapToTransactionReportItem(Order order) {
        TransactionReportItemResponse item = new TransactionReportItemResponse();
        item.setOrderId(order.getId());
        item.setOrderCode(order.getOrderCode());
        item.setStatus(order.getStatus().name());
        item.setCreatedAt(order.getCreatedAt());
        item.setPaidAt(order.getPaidAt());
        item.setTotalPrice(order.getTotalPrice());

        // Buyer info (Sprint 1: only userId)
        TransactionReportItemResponse.BuyerInfo buyer = new TransactionReportItemResponse.BuyerInfo();
        buyer.setUserId(order.getUserId());
        // For now, these are placeholder or we could try to get from another service in Phase 2
        String userId = order.getUserId();
        String displayId = userId != null && userId.length() > 5 ? userId.substring(0, 5) : (userId != null ? userId : "Unknown");
        buyer.setUsername("User " + displayId); 
        item.setBuyer(buyer);

        // Courses info
        item.setCourses(order.getItems().stream().map(orderItem -> {
            TransactionReportItemResponse.CourseInfo course = new TransactionReportItemResponse.CourseInfo();
            course.setPackageId(orderItem.getPackageId());
            course.setPackageName(orderItem.getPackageName());
            course.setPrice(orderItem.getPrice());
            course.setThumbnail(orderItem.getThumbnail());
            return course;
        }).collect(Collectors.toList()));

        return item;
    }
}
