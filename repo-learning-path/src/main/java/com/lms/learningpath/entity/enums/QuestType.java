package com.lms.learningpath.entity.enums;

public enum QuestType {
    DAILY,          // Quest hàng ngày (reset 00:00)
    WEEKLY,         // Quest hàng tuần (reset Monday)
    MONTHLY,        // Quest hàng tháng
    PROGRESSION,    // Quest theo tiến trình (1 lần)
    EVENT           // Quest sự kiện đặc biệt
}