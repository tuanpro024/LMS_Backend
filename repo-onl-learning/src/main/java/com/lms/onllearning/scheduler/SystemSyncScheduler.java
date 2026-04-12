package com.lms.onllearning.scheduler;

import com.lms.onllearning.dto.response.SyncAllJobResponse;
import com.lms.onllearning.exception.SyncAllAlreadyRunningException;
import com.lms.onllearning.service.ISystemSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "system-sync.auto-sync", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SystemSyncScheduler {

    private final ISystemSyncService systemSyncService;

    @Scheduled(cron = "${system-sync.auto-sync.cron:0 0 1 * * *}", zone = "${system-sync.auto-sync.zone:Asia/Ho_Chi_Minh}")
    public void triggerNightlySyncAll() {
        try {
            SyncAllJobResponse job = systemSyncService.startSyncAll();
            log.info("[SyncAllScheduler] Triggered nightly syncAll jobId={}", job.jobId());
        } catch (SyncAllAlreadyRunningException ex) {
            log.info("[SyncAllScheduler] Skip auto-sync because another job is running: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("[SyncAllScheduler] Failed to trigger nightly syncAll", ex);
        }
    }
}
