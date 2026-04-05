package com.lms.onllearning.service;

public interface ITimetableSyncService {
    /** Sync snapshot timetable toàn hệ thống từ CMS vào DB local. */
    void syncAll();
}
