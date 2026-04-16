package com.lms.onllearning.listener;

import com.lms.common.event.PackageStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Lắng nghe sự kiện Package bị đổi trạng thái (DRAFT / PUBLISHED).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PackageStatusKafkaListener {

    @KafkaListener(topics = "${app.kafka.topics.package-status-events:package.status.events}")
    public void handlePackageStatusEvent(PackageStatusEvent event) {
        log.info("Received PackageStatusEvent: packageId={}, status={}, changedBy={}, reason={}",
                event.packageId(), event.status(), event.changedBy(), event.reason());

        if ("DRAFT".equals(event.status())) {
            log.info("Package {} reverted to DRAFT. Progress will be preserved in Online Learning Service.",
                    event.packageId());
            // Đảm bảo progress không bị mất.
        }
    }
}