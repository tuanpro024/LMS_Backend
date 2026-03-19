package com.lms.onllearning.service;

public interface ISyllabusSyncService {
    /** Sync toàn bộ syllabus từ CMS vào DB (list + detail cho từng syllabus) */
    void syncAll();

    /** Sync chi tiết 1 syllabus theo id */
    void syncOne(String syllabusId);
}
