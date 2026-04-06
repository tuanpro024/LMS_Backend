package com.lms.onllearning.dto.response;

/**
 * Envelope chuẩn cho mọi response liên quan đến CMS.
 * Frontend dựa vào meta để hiển thị thông báo phù hợp khi CMS không khả dụng.
 *
 * @param data Dữ liệu trả về (null nếu cmsUnavailable = true)
 * @param meta Metadata về nguồn gốc dữ liệu
 */
public record CmsEnvelope<T>(
        T data,
        Meta meta) {
    public record Meta(
            /** "CMS" nếu lấy trực tiếp, "CACHE" nếu lấy từ Redis */
            String source,
            /** true nếu CMS down và đang trả dữ liệu cũ từ cache */
            boolean isStale,
            /**
             * true nếu CMS down và không có cache — frontend hiển thị "Không thể tải dữ
             * liệu"
             */
            boolean cmsUnavailable,
            /** (chỉ cho timetable) true nếu CMS trả 404 - học viên chưa có lịch */
            Boolean scheduled) {
    }

    public static <T> CmsEnvelope<T> fromCms(T data) {
        return new CmsEnvelope<>(data, new Meta("CMS", false, false, null));
    }

    public static <T> CmsEnvelope<T> fromCache(T data) {
        return new CmsEnvelope<>(data, new Meta("CACHE", true, false, null));
    }

    public static <T> CmsEnvelope<T> fromDb(T data) {
        return new CmsEnvelope<>(data, new Meta("DB", false, false, null));
    }

    public static <T> CmsEnvelope<T> cmsUnavailable() {
        return new CmsEnvelope<>(null, new Meta("CACHE", false, true, null));
    }

    public static <T> CmsEnvelope<T> notFound() {
        return new CmsEnvelope<>(null, new Meta("DB", false, false, null));
    }

    public static <T> CmsEnvelope<T> noSchedule(T emptyData) {
        return new CmsEnvelope<>(emptyData, new Meta("CMS", false, false, false));
    }

    public static <T> CmsEnvelope<T> noScheduleFromDb(T emptyData) {
        return new CmsEnvelope<>(emptyData, new Meta("DB", false, false, false));
    }
}
