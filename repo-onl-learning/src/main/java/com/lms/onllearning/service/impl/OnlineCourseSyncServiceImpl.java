package com.lms.onllearning.service.impl;

import com.lms.onllearning.client.CmsClient;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.CmsOnlineCourseResponse;
import com.lms.onllearning.entity.OnlineCourse;
import com.lms.onllearning.repository.OnlineCourseRepository;
import com.lms.onllearning.service.IOnlineCourseSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineCourseSyncServiceImpl implements IOnlineCourseSyncService {

    private static final String TYPE_ONE_ONE = "1-1";
    private static final String TYPE_GROUP = "GROUP";

    private final CmsClient cmsClient;
    private final OnlineCourseRepository courseRepo;

    @Scheduled(cron = "0 15 3 * * *")
    public void scheduledSync() {
        log.info("[OnlineCourseSync] Bắt đầu scheduled sync...");
        syncAll();
    }

    @Override
    @Transactional
    public void syncAll() {
        CmsEnvelope<List<CmsOnlineCourseResponse>> oneOneEnvelope = cmsClient.getOnlineCoursesByType(TYPE_ONE_ONE);
        CmsEnvelope<List<CmsOnlineCourseResponse>> groupEnvelope = cmsClient.getOnlineCoursesByType(TYPE_GROUP);

        List<CmsOnlineCourseResponse> merged = new ArrayList<>();
        merged.addAll(extractData(oneOneEnvelope));
        merged.addAll(extractData(groupEnvelope));

        if (merged.isEmpty() && (oneOneEnvelope.meta().cmsUnavailable() || groupEnvelope.meta().cmsUnavailable())) {
            log.warn("[OnlineCourseSync] CMS không khả dụng, bỏ qua syncAll");
            return;
        }

        List<String> cmsIds = new ArrayList<>();
        for (CmsOnlineCourseResponse item : merged) {
            if (item == null || item.id() == null || item.id().isBlank()) {
                continue;
            }
            cmsIds.add(item.id());
            upsertCourse(item);
        }

        if (cmsIds.isEmpty()) {
            courseRepo.softDeleteAllCmsCourses();
        } else {
            courseRepo.softDeleteCmsCoursesNotIn(cmsIds);
        }

        log.info("[OnlineCourseSync] syncAll hoàn tất. totalCmsCourses={}", cmsIds.size());
    }

    @Override
    @Transactional
    public void syncByType(String type) {
        String normalizedType = normalizeType(type);
        CmsEnvelope<List<CmsOnlineCourseResponse>> envelope = cmsClient.getOnlineCoursesByType(normalizedType);
        List<CmsOnlineCourseResponse> data = extractData(envelope);

        if (data.isEmpty() && envelope.meta().cmsUnavailable()) {
            log.warn("[OnlineCourseSync] CMS không khả dụng với type={}, bỏ qua", normalizedType);
            return;
        }

        for (CmsOnlineCourseResponse item : data) {
            if (item == null || item.id() == null || item.id().isBlank()) {
                continue;
            }
            upsertCourse(item);
        }

        log.info("[OnlineCourseSync] syncByType hoàn tất. type={}, total={}", normalizedType, data.size());
    }

    private List<CmsOnlineCourseResponse> extractData(CmsEnvelope<List<CmsOnlineCourseResponse>> envelope) {
        return envelope != null && envelope.data() != null ? envelope.data() : List.of();
    }

    private String normalizeType(String type) {
        if (TYPE_ONE_ONE.equalsIgnoreCase(type)) {
            return TYPE_ONE_ONE;
        }
        return TYPE_GROUP;
    }

    private void upsertCourse(CmsOnlineCourseResponse src) {
        OnlineCourse entity = courseRepo.findById(src.id()).orElse(new OnlineCourse());
        entity.setId(src.id());
        entity.setCode(src.code());
        entity.setName(src.name());
        entity.setCourseType(src.type());
        entity.setLevel(src.level());
        entity.setTotalLessons(src.totalLessons());
        entity.setSyllabusId(src.syllabusId());
        entity.setPrice(src.price());
        entity.setCmsSynced(true);
        entity.setDeleted(false);
        courseRepo.save(entity);
    }
}
