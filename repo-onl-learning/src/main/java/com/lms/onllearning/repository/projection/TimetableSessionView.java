package com.lms.onllearning.repository.projection;

public interface TimetableSessionView {
    String getId();

    String getClassId();

    String getSyllabusScheduleId();

    String getClassCode();

    String getClassName();

    Integer getSessionNo();

    String getTitle();

    String getDate();

    String getSessionDate();

    String getStartTime();

    String getEndTime();

    String getStatus();

    String getSessionStatus();

    Boolean getIsExam();

    String getTutorId();

    String getTutorEmail();

    String getTutorName();

    String getInstructorName();

    String getClassRoom();

    String getAttendanceStatus();

    String getCheckInAt();

    String getExamId();

    String getExamType();

    String getExamTitle();

    String getExamDatetime();

    String getExamLink();

    String getExamNote();
}
