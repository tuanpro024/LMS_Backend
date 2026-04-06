package com.lms.payment.event;

import com.lms.payment.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Internal Spring event to trigger notification after transaction commit.
 */
@Getter
public class PaymentCompletedInternalEvent extends ApplicationEvent {
    private final Order order;

    public PaymentCompletedInternalEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
