package com.lms.videocourse.config;

import com.lms.videocourse.entity.enums.SyncTriggerType;
import com.lms.videocourse.service.SyllabusSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job to sync syllabus data from CMS at a configurable cron interval.
 * Defaults to 03:00 daily (Asia/Bangkok timezone).
 * Can be disabled via: app.syllabus-sync.enabled=false
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.syllabus-sync.enabled", havingValue = "true", matchIfMissing = false)
public class SyllabusSyncScheduler {

    private final SyllabusSyncService syncService;

    @Scheduled(cron = "${app.syllabus-sync.cron}", zone = "${app.syllabus-sync.zone:Asia/Bangkok}")
    public void scheduledSync() {
        log.info("[Scheduler] Starting scheduled syllabus sync");
        try {
            syncService.syncAll(SyncTriggerType.SCHEDULED);
            log.info("[Scheduler] Scheduled syllabus sync completed");
        } catch (Exception e) {
            log.error("[Scheduler] Scheduled sync failed: {}", e.getMessage(), e);
        }
    }
}
