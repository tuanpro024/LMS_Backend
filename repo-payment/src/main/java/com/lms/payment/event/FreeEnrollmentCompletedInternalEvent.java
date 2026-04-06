package com.lms.payment.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Internal Spring event to trigger notification after free enrollment.
 */
@Getter
public class FreeEnrollmentCompletedInternalEvent extends ApplicationEvent {
    private final String userId;
    private final String packageId;
    private final String packageName;
    private final String thumbnail;

    public FreeEnrollmentCompletedInternalEvent(Object source, String userId, String packageId, String packageName, String thumbnail) {
        super(source);
        this.userId = userId;
        this.packageId = packageId;
        this.packageName = packageName;
        this.thumbnail = thumbnail;
    }
}
