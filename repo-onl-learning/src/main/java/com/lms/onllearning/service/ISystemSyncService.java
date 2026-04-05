package com.lms.onllearning.service;

import com.lms.onllearning.dto.response.SyncAllJobResponse;

public interface ISystemSyncService {
    SyncAllJobResponse startSyncAll();

    SyncAllJobResponse getJob(String jobId);

    SyncAllJobResponse getCurrentJob();
}
