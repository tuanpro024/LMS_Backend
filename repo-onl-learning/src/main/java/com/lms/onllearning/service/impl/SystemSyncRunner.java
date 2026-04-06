package com.lms.onllearning.service.impl;

import com.lms.onllearning.service.IOnlineCourseSyncService;
import com.lms.onllearning.service.ISyllabusSyncService;
import com.lms.onllearning.service.ITimetableSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSyncRunner {

    private final ISyllabusSyncService syllabusSyncService;
    private final IOnlineCourseSyncService onlineCourseSyncService;
    private final ITimetableSyncService timetableSyncService;

    @Async("syncAllTaskExecutor")
    public void runJob(String jobId, JobStatusCallback statusCallback) {
        try {
            statusCallback.markStep("SYLLABUS", 10, "Đang đồng bộ syllabus...");
            syllabusSyncService.syncAll();

            statusCallback.markStep("COURSE", 50, "Đang đồng bộ courses...");
            onlineCourseSyncService.syncAll();

            statusCallback.markStep("TIMETABLE", 80, "Đang đồng bộ timetable...");
            timetableSyncService.syncAll();

            statusCallback.markSuccess("Đồng bộ tất cả dữ liệu thành công");
        } catch (Exception ex) {
            log.error("[SyncAll] Job {} failed", jobId, ex);
            statusCallback.markFailed("Đồng bộ thất bại: " + ex.getMessage());
        }
    }

    public interface JobStatusCallback {
        void markStep(String step, int progressPercent, String message);

        void markSuccess(String message);

        void markFailed(String message);
    }
}
