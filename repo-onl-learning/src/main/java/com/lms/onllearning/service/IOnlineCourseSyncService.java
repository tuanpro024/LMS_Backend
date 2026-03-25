package com.lms.onllearning.service;

public interface IOnlineCourseSyncService {
    void syncAll();

    void syncByType(String type);
}
