package com.lms.payment.listener;

import com.lms.common.notification.NotificationEvent;
import com.lms.common.notification.NotificationPublisher;
import com.lms.common.notification.ResourceType;
import com.lms.payment.entity.Order;
import com.lms.payment.entity.OrderItem;
import com.lms.payment.event.PaymentCompletedInternalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Listens for PaymentCompletedInternalEvent and publishes a cross-service notification
 * ONLY after the payment transaction has successfully committed.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchListener {

    private final NotificationPublisher notificationPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedInternalEvent event) {
        Order order = event.getOrder();
        log.info("Processing notification for successful payment of order: {}", order.getId());

        List<OrderItem> items = order.getItems();
        int itemCount = items.size();

        // Check if this is a membership purchase
        boolean isMembership = items.stream()
                .anyMatch(i -> i.getItemType() == com.lms.payment.entity.enums.ItemType.MEMBERSHIP);

        String title;
        String message;
        String eventType;
        String targetUrl;

        if (isMembership) {
            title = "Nâng cấp Premium thành công";
            message = "Chào mừng bạn đến với gói thành viên Học Bá. Mọi đặc quyền của bạn đã được kích hoạt!";
            eventType = "MEMBERSHIP_PURCHASE_SUCCESS";
            targetUrl = "/membership";
        } else {
            title = "Mua khóa học thành công";
            message = itemCount == 1
                    ? "Bạn đã mua thành công khóa học " + items.get(0).getPackageName()
                    : "Bạn đã mua thành công " + itemCount + " khóa học video course";
            eventType = "VIDEO_COURSE_PURCHASE_SUCCESS";
            targetUrl = itemCount == 1
                    ? "/video-course?packageId=" + items.get(0).getPackageId()
                    : "/profile/orders/" + order.getId();
        }

        // Build Payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", order.getId());
        payload.put("orderCode", order.getOrderCode());
        payload.put("targetUrl", targetUrl);
        payload.put("itemCount", itemCount);
        payload.put("isMembership", isMembership);
        payload.put("items", items.stream().map(item -> Map.of(
                "packageId", item.getPackageId(),
                "packageName", item.getPackageName(),
                "thumbnail", item.getThumbnail() != null ? item.getThumbnail() : ""
        )).collect(Collectors.toList()));

        // Resource identifier
        String resourceId = itemCount == 1 ? items.get(0).getPackageId() : order.getId();

        // Publish to Kafka
        notificationPublisher.publish(new NotificationEvent(
                order.getUserId(),
                eventType,
                title,
                message,
                ResourceType.OTHER,
                resourceId,
                payload,
                "payment:order:" + order.getId() + ":purchase-success"
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFreeEnrollmentCompleted(com.lms.payment.event.FreeEnrollmentCompletedInternalEvent event) {
        log.info("Processing notification for successful free enrollment: userId={}, packageId={}", 
                event.getUserId(), event.getPackageId());

        String packageName = event.getPackageName();
        String title = "Đăng ký khóa học thành công";
        String message = "Bạn đã đăng ký thành công khóa học " + packageName;

        String targetUrl = "/video-course?packageId=" + event.getPackageId();

        // Build Payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("packageId", event.getPackageId());
        payload.put("packageName", packageName);
        payload.put("targetUrl", targetUrl);
        payload.put("thumbnail", event.getThumbnail() != null ? event.getThumbnail() : "");

        // Publish to Kafka
        notificationPublisher.publish(new NotificationEvent(
                event.getUserId(),
                "VIDEO_COURSE_FREE_ENROLL_SUCCESS",
                title,
                message,
                ResourceType.OTHER,
                event.getPackageId(),
                payload,
                "payment:enroll:" + event.getUserId() + ":" + event.getPackageId() + ":success"
        ));
    }
}
