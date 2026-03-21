package com.lms.onllearning.service.impl;

import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.SyllabusDetailResponse;
import com.lms.onllearning.dto.response.SyllabusResponse;
import com.lms.onllearning.entity.Syllabus;
import com.lms.onllearning.entity.SyllabusClo;
import com.lms.onllearning.entity.SyllabusGrading;
import com.lms.onllearning.entity.SyllabusMaterial;
import com.lms.onllearning.entity.SyllabusSchedule;
import com.lms.onllearning.repository.SyllabusCloRepository;
import com.lms.onllearning.repository.SyllabusGradingRepository;
import com.lms.onllearning.repository.SyllabusMaterialRepository;
import com.lms.onllearning.repository.SyllabusRepository;
import com.lms.onllearning.repository.SyllabusScheduleRepository;
import com.lms.onllearning.service.ISyllabusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Đọc dữ liệu Syllabus từ DB local (đã được đồng bộ từ CMS qua SyllabusSyncService).
 * Không gọi CMS trực tiếp — tách riêng concern sync vs. read.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SyllabusServiceImpl implements ISyllabusService {

    private final SyllabusRepository syllabusRepo;
    private final SyllabusCloRepository cloRepo;
    private final SyllabusMaterialRepository materialRepo;
    private final SyllabusScheduleRepository scheduleRepo;
    private final SyllabusGradingRepository gradingRepo;

    @Override
    public CmsEnvelope<List<SyllabusResponse>> getAllSyllabuses() {
        List<Syllabus> entities = syllabusRepo.findAllByDeletedFalseOrderByCreatedAtDesc();
        if (entities.isEmpty()) {
            log.warn("[SyllabusService] DB trống — chưa sync từ CMS. Hãy gọi POST /syllabuses/sync");
        }
        List<SyllabusResponse> list = entities.stream()
                .map(this::toSyllabusResponse)
                .toList();
        return CmsEnvelope.fromDb(list);
    }

    @Override
    public CmsEnvelope<SyllabusDetailResponse> getSyllabusDetail(String syllabusId) {
        return syllabusRepo.findById(syllabusId)
                .filter(s -> !s.isDeleted())
                .map(s -> {
                    SyllabusDetailResponse detail = toSyllabusDetailResponse(s);
                    return CmsEnvelope.fromDb(detail);
                })
                .orElseGet(() -> {
                    log.warn("[SyllabusService] Không tìm thấy syllabus id={} trong DB", syllabusId);
                    return CmsEnvelope.notFound();
                });
    }

    // -----------------------------------------------------------------------
    // Mappers: Entity → DTO
    // -----------------------------------------------------------------------

    private SyllabusResponse toSyllabusResponse(Syllabus s) {
        return new SyllabusResponse(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getHskLevel(),
                s.getType(),
                s.getStatus() != null ? s.getStatus().name() : null,
                s.getAuthor(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    private SyllabusDetailResponse toSyllabusDetailResponse(Syllabus s) {
        List<SyllabusClo> clos       = cloRepo.findBySyllabusId(s.getId());
        List<SyllabusMaterial> mats  = materialRepo.findBySyllabusId(s.getId());
        List<SyllabusSchedule> scheds = scheduleRepo.findBySyllabusId(s.getId());
        List<SyllabusGrading> grades  = gradingRepo.findBySyllabusId(s.getId());

        return new SyllabusDetailResponse(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getHskLevel(),
                s.getType(),
                s.getVersion(),
                s.getAuthor(),
                s.getDescription(),
                s.getDistributionHours(),
                s.getDocumentType(),
                s.getPrerequisite(),
                s.getTrainingProgram(),
                s.getStudentResponsibilities(),
                s.getMinimumPassingScore(),
                s.getScoreRange(),
                s.getNotes(),
                s.getStatus() != null ? s.getStatus().name() : null,
                null, // teachingMethods — stored as JSON string, parse nếu cần
                null, // learningTools   — stored as JSON string, parse nếu cần
                s.getTotalSessions(),
                clos.stream().map(c -> new SyllabusDetailResponse.CloItem(
                        c.getId(), s.getId(), c.getName(), c.getDescription()
                )).toList(),
                mats.stream().map(m -> new SyllabusDetailResponse.MaterialItem(
                        m.getId(), s.getId(), m.getTitle(), m.getType(),
                        m.getIsbn(), m.getAuthor(), m.getPublisher(), m.getYear(), m.getEdition()
                )).toList(),
                scheds.stream().map(sc -> new SyllabusDetailResponse.ScheduleItem(
                        sc.getId(), s.getId(), sc.getSessionNo(), sc.getTopic(), sc.getContent(),
                        sc.getDelivery(), sc.getSessionLo(), sc.getCoreClo(), sc.getSupportingClo(),
                        sc.getEvidence(), sc.getItu(), sc.getStudentMaterials(), sc.getTeacherMaterials(),
                        sc.getStudentTasks(), sc.getTeacherTasks(), sc.getStudentMaterialsLink(),
                        sc.getTeacherMaterialsLink(), sc.getModuleOnLuyen()
                )).toList(),
                grades.stream().map(g -> new SyllabusDetailResponse.GradingItem(
                        g.getId(), s.getId(), g.getItem(), g.getType(), g.getWeight(),
                        g.getTiming(), g.getDuration(), g.getClo(), g.getOrganizationalForm(),
                        g.getCriteria(), g.getContentScope()
                )).toList()
        );
    }
}
