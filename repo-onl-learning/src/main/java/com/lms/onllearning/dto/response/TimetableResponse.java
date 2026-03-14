package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Maps to CMS timetable item response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TimetableResponse(
    String id,
    String classId,
    Integer sessionNo,
    LocalDate date,
    String title,
    Long syllabusScheduleId,
    LocalTime startTime,
    LocalTime endTime,
    String status,        // "COMPLETED" | "PLANING"
    Boolean isExam,
    String tutorId,
    String homeworkAssigned,
    String contentDelivered,
    String zoomLink,
    String note
) {}
