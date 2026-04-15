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
    private final com.lms.payment.client.IdentityClient identityClient;

    public PaymentReportServiceImpl(OrderRepository orderRepository, com.lms.payment.client.IdentityClient identityClient) {
        this.orderRepository = orderRepository;
        this.identityClient = identityClient;
    }

    @Override
    public PaymentSummaryResponse getSummary() {
        BigDecimal totalRevenue = orderRepository.sumTotalPriceByStatus(OrderStatus.COMPLETED);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        long totalTransactions = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long totalStudents = orderRepository.countDistinctUserIdByStatus(OrderStatus.COMPLETED);
        long totalCoursesSold = orderRepository.countTotalItemsByStatusAndItemType(OrderStatus.COMPLETED, com.lms.payment.entity.enums.ItemType.COURSE);
        long totalMembershipsSold = orderRepository.countTotalItemsByStatusAndItemType(OrderStatus.COMPLETED, com.lms.payment.entity.enums.ItemType.MEMBERSHIP);

        return PaymentSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTransactions(totalTransactions)
                .totalStudents(totalStudents)
                .totalCoursesSold(totalCoursesSold)
                .totalMembershipsSold(totalMembershipsSold)
                .build();
    }

    @Override
    public Page<TransactionReportItemResponse> getTransactionReport(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        
        // Fetch user profiles in batch
        java.util.List<String> userIds = orders.getContent().stream()
                .map(Order::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        java.util.Map<String, com.lms.payment.client.dto.UserProfileDto> profileMap = new java.util.HashMap<>();
        try {
            com.lms.common.dto.ApiResponse<java.util.List<com.lms.payment.client.dto.UserProfileDto>> response = identityClient.getProfilesBatch(userIds);
            if (response != null && response.success() && response.data() != null) {
                for (com.lms.payment.client.dto.UserProfileDto profile : response.data()) {
                    profileMap.put(profile.getId(), profile);
                }
            }
        } catch (Exception e) {
            // Log and continue with partial data
        }

        return orders.map(order -> mapToTransactionReportItem(order, profileMap.get(order.getUserId())));
    }

    private TransactionReportItemResponse mapToTransactionReportItem(Order order, com.lms.payment.client.dto.UserProfileDto profile) {
        TransactionReportItemResponse item = new TransactionReportItemResponse();
        item.setOrderId(order.getId());
        item.setOrderCode(order.getOrderCode());
        item.setStatus(order.getStatus().name());
        item.setCreatedAt(order.getCreatedAt());
        item.setPaidAt(order.getPaidAt());
        item.setTotalPrice(order.getTotalPrice());

        // Buyer info
        TransactionReportItemResponse.BuyerInfo buyer = new TransactionReportItemResponse.BuyerInfo();
        buyer.setUserId(order.getUserId());
        
        if (profile != null) {
            buyer.setUsername(profile.getEmail()); // Or profile.getUsername() if existed
            buyer.setFullName(profile.getFullName());
            buyer.setEmail(profile.getEmail());
        } else {
            String userId = order.getUserId();
            String displayId = userId != null && userId.length() > 5 ? userId.substring(0, 5) : (userId != null ? userId : "Unknown");
            buyer.setUsername("User " + displayId); 
        }
        item.setBuyer(buyer);

        // Courses info
        item.setCourses(order.getItems().stream().map(orderItem -> {
            TransactionReportItemResponse.CourseInfo course = new TransactionReportItemResponse.CourseInfo();
            course.setPackageId(orderItem.getPackageId());
            course.setPackageName(orderItem.getPackageName());
            course.setPrice(orderItem.getPrice());
            course.setThumbnail(orderItem.getThumbnail());
            course.setItemType(orderItem.getItemType() != null ? orderItem.getItemType().name() : null);
            course.setDurationInDays(orderItem.getDurationInDays());
            return course;
        }).collect(Collectors.toList()));

        return item;
    }
}
