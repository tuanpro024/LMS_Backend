package com.lms.onllearning.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.onllearning.client.CmsClient;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.SyllabusDetailResponse;
import com.lms.onllearning.dto.response.SyllabusResponse;
import com.lms.onllearning.entity.*;
import com.lms.onllearning.repository.*;
import com.lms.onllearning.service.ISyllabusSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Đồng bộ dữ liệu syllabus từ CMS vào DB local (fallback khi CMS down).
 * Dự trù: scheduled hàng ngày lúc 3h sáng + trigger thủ công qua API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SyllabusSyncServiceImpl implements ISyllabusSyncService {

    private final CmsClient cmsClient;
    private final SyllabusRepository syllabusRepo;
    private final SyllabusCloRepository cloRepo;
    private final SyllabusMaterialRepository materialRepo;
    private final SyllabusScheduleRepository scheduleRepo;
    private final SyllabusGradingRepository gradingRepo;
    private final ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // Scheduled — mỗi ngày lúc 03:00 AM
    // -----------------------------------------------------------------------

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledSync() {
        log.info("[SyllabusSync] Bắt đầu scheduled sync...");
        syncAll();
    }

    // -----------------------------------------------------------------------
    // Public methods
    // -----------------------------------------------------------------------

    @Override
    public void syncAll() {
        CmsEnvelope<List<SyllabusResponse>> envelope = cmsClient.getSyllabuses();
        if (envelope.meta().cmsUnavailable() || envelope.data() == null) {
            log.warn("[SyllabusSync] CMS không khả dụng, bỏ qua syncAll");
            return;
        }

        List<SyllabusResponse> list = envelope.data();
        log.info("[SyllabusSync] Đồng bộ {} syllabus đầu (list only)...", list.size());

        for (SyllabusResponse sr : list) {
            try {
                upsertSyllabusHeader(sr);
            } catch (Exception e) {
                log.error("[SyllabusSync] Lỗi upsert syllabus list id={}: {}", sr.id(), e.getMessage());
            }
        }

        // Sau khi upsert header xong, sync chi tiết từng cái
        for (SyllabusResponse sr : list) {
            try {
                syncOne(sr.id());
            } catch (Exception e) {
                log.error("[SyllabusSync] Lỗi syncOne id={}: {}", sr.id(), e.getMessage());
            }
        }
        log.info("[SyllabusSync] syncAll hoàn tất.");
    }

    @Override
    @Transactional
    public void syncOne(String syllabusId) {
        CmsEnvelope<SyllabusDetailResponse> envelope = cmsClient.getSyllabusDetail(syllabusId);
        if (envelope.meta().cmsUnavailable() || envelope.data() == null) {
            log.warn("[SyllabusSync] CMS không khả dụng cho syllabusId={}", syllabusId);
            return;
        }
        SyllabusDetailResponse detail = envelope.data();
        upsertSyllabusDetail(detail);
        log.info("[SyllabusSync] Đã sync syllabus id={}", syllabusId);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /** Upsert chỉ header (fields từ list API) */
    private void upsertSyllabusHeader(SyllabusResponse sr) {
        Syllabus entity = syllabusRepo.findById(sr.id()).orElse(new Syllabus());
        entity.setId(sr.id());
        entity.setCode(sr.code());
        entity.setName(sr.name());
        entity.setHskLevel(sr.hskLevel());
        entity.setType(sr.type());
        entity.setAuthor(sr.author());
        entity.setStatus(parseSyllabusStatus(sr.status()));
        entity.setCreatedAt(sr.createdAt());
        entity.setUpdatedAt(sr.updatedAt());
        syllabusRepo.save(entity);
    }

    /** Upsert đầy đủ syllabus + xóa & tạo lại clo / material / schedule / grading */
    @Transactional
    protected void upsertSyllabusDetail(SyllabusDetailResponse d) {
        // --- 1. Syllabus header ---
        Syllabus entity = syllabusRepo.findById(d.id()).orElse(new Syllabus());
        entity.setId(d.id());
        entity.setCode(d.code());
        entity.setName(d.name());
        entity.setHskLevel(d.hskLevel());
        entity.setType(d.type());
        entity.setVersion(d.version());
        entity.setAuthor(d.author());
        entity.setDescription(d.description());
        entity.setDistributionHours(d.distributionHours());
        entity.setDocumentType(d.documentType());
        entity.setPrerequisite(d.prerequisite());
        entity.setTrainingProgram(d.trainingProgram());
        entity.setStudentResponsibilities(d.studentResponsibilities());
        entity.setMinimumPassingScore(d.minimumPassingScore());
        entity.setScoreRange(d.scoreRange());
        entity.setNotes(d.notes());
        entity.setTeachingMethods(toJson(d.teachingMethods()));
        entity.setLearningTools(toJson(d.learningTools()));
        entity.setStatus(parseSyllabusStatus(d.status()));
        entity.setTotalSessions(d.totalSessions());
        Syllabus saved = syllabusRepo.save(entity);

        // --- 2. CLO ---
        cloRepo.deleteBySyllabusId(saved.getId());
        if (d.clo() != null) {
            for (SyllabusDetailResponse.CloItem c : d.clo()) {
                cloRepo.save(SyllabusClo.builder()
                        .id(c.id())
                        .syllabus(saved)
                        .name(c.name())
                        .description(c.description())
                        .build());
            }
        }

        // --- 3. Material ---
        materialRepo.deleteBySyllabusId(saved.getId());
        if (d.materials() != null) {
            for (SyllabusDetailResponse.MaterialItem m : d.materials()) {
                materialRepo.save(SyllabusMaterial.builder()
                        .id(m.id())
                        .syllabus(saved)
                        .title(m.title())
                        .type(m.type())
                        .isbn(m.isbn())
                        .author(m.author())
                        .publisher(m.publisher())
                        .year(m.year())
                        .edition(m.edition())
                        .build());
            }
        }

        // --- 4. Schedule ---
        scheduleRepo.deleteBySyllabusId(saved.getId());
        if (d.schedule() != null) {
            for (SyllabusDetailResponse.ScheduleItem s : d.schedule()) {
                scheduleRepo.save(SyllabusSchedule.builder()
                        .id(s.id())
                        .syllabus(saved)
                        .sessionNo(s.sessionNo())
                        .topic(s.topic())
                        .content(s.content())
                        .delivery(s.delivery())
                        .sessionLo(s.sessionLo())
                        .coreClo(s.coreClo())
                        .supportingClo(s.supportingClo())
                        .evidence(s.evidence())
                        .itu(s.itu())
                        .studentMaterials(s.studentMaterials())
                        .teacherMaterials(s.teacherMaterials())
                        .studentTasks(s.studentTasks())
                        .teacherTasks(s.teacherTasks())
                        .studentMaterialsLink(s.studentMaterialsLink())
                        .teacherMaterialsLink(s.teacherMaterialsLink())
                        .moduleOnLuyen(s.moduleOnLuyenText())
                        .build());
            }
        }

        // --- 5. Grading ---
        gradingRepo.deleteBySyllabusId(saved.getId());
        if (d.gradingStructure() != null) {
            for (SyllabusDetailResponse.GradingItem g : d.gradingStructure()) {
                gradingRepo.save(SyllabusGrading.builder()
                        .id(g.id())
                        .syllabus(saved)
                        .item(g.item())
                        .type(g.type())
                        .weight(g.weight())
                        .timing(g.timing())
                        .duration(g.duration())
                        .clo(g.clo())
                        .organizationalForm(g.organizationalForm())
                        .criteria(g.criteria())
                        .contentScope(g.contentScope())
                        .build());
            }
        }
    }

    private Syllabus.SyllabusStatus parseSyllabusStatus(String status) {
        if (status == null) return Syllabus.SyllabusStatus.DRAFT;
        try {
            return Syllabus.SyllabusStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Syllabus.SyllabusStatus.DRAFT;
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Không thể serialize sang JSON: {}", e.getMessage());
            return null;
        }
    }
}
