package com.lms.content.common.entity;

import lombok.Getter;

@Getter
public enum CategoryType {
    EXAM_PREPARATION("Khóa ôn thi Tiếng Trung"),
    FREE_LEARNING("Khóa học Tiếng Trung tự do"),
    HSK("HSK"),
    BUSINESS_CHINESE("Tiếng Trung cho người đi làm");

    private final String displayName;

    CategoryType(String displayName) {
        this.displayName = displayName;
    }
}
