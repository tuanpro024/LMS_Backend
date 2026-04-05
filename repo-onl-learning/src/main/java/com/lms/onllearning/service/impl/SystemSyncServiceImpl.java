package com.lms.onllearning.service.impl;

import com.lms.onllearning.dto.response.SyncAllJobResponse;
import com.lms.onllearning.exception.SyncAllAlreadyRunningException;
import com.lms.onllearning.exception.SyncAllJobNotFoundException;
import com.lms.onllearning.service.ISystemSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class SystemSyncServiceImpl implements ISystemSyncService {

    private final SystemSyncRunner systemSyncRunner;

    private final AtomicReference<String> runningJobId = new AtomicReference<>();
    private final Map<String, JobState> jobs = new ConcurrentHashMap<>();

    @Override
    public SyncAllJobResponse startSyncAll() {
        String current = runningJobId.get();
        if (current != null) {
            JobState running = jobs.get(current);
            String message = running == null
                    ? "Đã có sync all đang chạy"
                    : "Đã có sync all đang chạy với jobId=" + running.jobId;
            throw new SyncAllAlreadyRunningException(message);
        }

        String jobId = UUID.randomUUID().toString();
        JobState state = new JobState(jobId);
        state.status = "RUNNING";
        state.currentStep = "QUEUED";
        state.progressPercent = 0;
        state.message = "Sync all đã được tạo";
        state.startedAt = Instant.now();

        jobs.put(jobId, state);
        if (!runningJobId.compareAndSet(null, jobId)) {
            jobs.remove(jobId);
            throw new SyncAllAlreadyRunningException("Đã có sync all đang chạy");
        }

        systemSyncRunner.runJob(jobId, new SystemSyncRunner.JobStatusCallback() {
            @Override
            public void markStep(String step, int progressPercent, String message) {
                SystemSyncServiceImpl.this.markStep(jobId, step, progressPercent, message);
            }

            @Override
            public void markSuccess(String message) {
                SystemSyncServiceImpl.this.markSuccess(jobId, message);
            }

            @Override
            public void markFailed(String message) {
                SystemSyncServiceImpl.this.markFailed(jobId, message);
            }
        });
        return toResponse(state);
    }

    @Override
    public SyncAllJobResponse getJob(String jobId) {
        JobState state = jobs.get(jobId);
        if (state == null) {
            throw new SyncAllJobNotFoundException("Không tìm thấy sync job id=" + jobId);
        }
        return toResponse(state);
    }

    @Override
    public SyncAllJobResponse getCurrentJob() {
        String current = runningJobId.get();
        if (current == null) {
            return null;
        }

        JobState state = jobs.get(current);
        if (state == null) {
            runningJobId.compareAndSet(current, null);
            return null;
        }
        return toResponse(state);
    }

    void markStep(String jobId, String step, int progressPercent, String message) {
        JobState state = requireJob(jobId);
        state.currentStep = step;
        state.progressPercent = progressPercent;
        state.message = message;
    }

    void markSuccess(String jobId, String message) {
        JobState state = requireJob(jobId);
        state.status = "SUCCESS";
        state.currentStep = "DONE";
        state.progressPercent = 100;
        state.message = message;
        state.finishedAt = Instant.now();
        runningJobId.compareAndSet(jobId, null);
    }

    void markFailed(String jobId, String message) {
        JobState state = requireJob(jobId);
        state.status = "FAILED";
        state.currentStep = "FAILED";
        state.message = message;
        state.finishedAt = Instant.now();
        runningJobId.compareAndSet(jobId, null);
    }

    private JobState requireJob(String jobId) {
        JobState state = jobs.get(jobId);
        if (state == null) {
            throw new SyncAllJobNotFoundException("Không tìm thấy sync job id=" + jobId);
        }
        return state;
    }

    private SyncAllJobResponse toResponse(JobState state) {
        return new SyncAllJobResponse(
                state.jobId,
                state.status,
                state.currentStep,
                state.progressPercent,
                state.message,
                state.startedAt,
                state.finishedAt);
    }

    private static class JobState {
        private final String jobId;
        private volatile String status;
        private volatile String currentStep;
        private volatile int progressPercent;
        private volatile String message;
        private volatile Instant startedAt;
        private volatile Instant finishedAt;

        private JobState(String jobId) {
            this.jobId = jobId;
        }
    }
}
