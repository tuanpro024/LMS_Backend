package com.lms.identity.service;

import com.lms.identity.client.TeacherClient;
import com.lms.identity.dto.response.CmsTeacherResponse;
import com.lms.identity.entity.ExternalTeacher;
import com.lms.identity.repository.ExternalTeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherSyncService {

    private final TeacherClient teacherClient;
    private final ExternalTeacherRepository externalTeacherRepository;

    /**
     * Đồng bộ dữ liệu từ CMS về bảng external_teachers.
     * Chạy tự động vào 3h sáng hàng ngày.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void syncTeachers() {
        log.info("[TeacherSync] Bắt đầu đồng bộ giảng viên từ CMS...");
        List<CmsTeacherResponse> cmsTeachers = teacherClient.getTeachers();
        
        if (cmsTeachers.isEmpty()) {
            log.warn("[TeacherSync] Không lấy được dữ liệu từ CMS hoặc danh sách trống.");
            return;
        }

        for (CmsTeacherResponse cmsTeacher : cmsTeachers) {
            try {
                upsertTeacher(cmsTeacher);
            } catch (Exception e) {
                log.error("[TeacherSync] Lỗi khi đồng bộ giảng viên {}: {}", cmsTeacher.email(), e.getMessage());
            }
        }
        
        log.info("[TeacherSync] Hoàn tất đồng bộ {} giảng viên.", cmsTeachers.size());
    }

    private void upsertTeacher(CmsTeacherResponse dto) {
        // Tìm theo ID CMS trước, nếu không có thì tìm theo Email, cuối cùng tạo mới
        ExternalTeacher entity = externalTeacherRepository.findByCmsUserId(dto.userId())
                .orElseGet(() -> externalTeacherRepository.findByEmail(dto.email())
                        .orElse(new ExternalTeacher()));

        // Cập nhật các trường thông tin
        entity.setCmsUserId(dto.userId());
        entity.setEmail(dto.email());
        entity.setFullName(dto.fullName());
        entity.setPhoneNumber(dto.phoneNumber());
        entity.setAddress(dto.address());
        entity.setAvatarUrl(dto.avatarUrl());
        entity.setCmsStatus(dto.status());
        entity.setQualification(dto.qualification());
        entity.setTeachingStyle(dto.teachingStyle());
        entity.setVideoIntroLink(dto.videoIntroLink());
        entity.setShortDescription(dto.shortDescription());
        entity.setFullDescription(dto.fullDescription());
        entity.setRating(dto.rating());
        entity.setTotalClasses(dto.totalClasses());
        entity.setTotalStudents(dto.totalStudents());
        entity.setTotalSessions(dto.totalSessions());

        // Hibernate sẽ tự động kiểm tra xem có trường nào thay đổi không (Dirty Checking).
        // Nếu bản ghi có sẵn và không có dữ liệu mới nào khác, lệnh save này sẽ không sinh ra câu lệnh UPDATE.
        externalTeacherRepository.save(entity);
    }

}
