package com.lms.onllearning.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.entity.TypeName;
import com.lms.onllearning.client.PracticeModuleWebClient;
import com.lms.onllearning.dto.request.AddModuleToScheduleRequest;
import com.lms.onllearning.dto.request.ImportModuleToScheduleRequest;
import com.lms.onllearning.dto.request.ReorderScheduleModulesRequest;
import com.lms.onllearning.dto.response.ScheduleModuleResponse;
import com.lms.onllearning.entity.ScheduleModule;
import com.lms.onllearning.entity.enums.ScheduleModuleType;
import com.lms.onllearning.repository.ScheduleModuleRepository;
import com.lms.onllearning.service.IScheduleModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleModuleServiceImpl implements IScheduleModuleService {

    private final ScheduleModuleRepository moduleRepo;
    private final PracticeModuleWebClient practiceWebClient;

    @Value("${practice.service.flashcard-url:http://repo-flashcard}")
    private String flashcardUrl;
    @Value("${practice.service.writing-url:http://repo-writing}")
    private String writingUrl;
    @Value("${practice.service.kanji-url:http://repo-kanji-origin}")
    private String kanjiUrl;
    @Value("${practice.service.pronunciation-url:http://repo-pronunciation}")
    private String pronunciationUrl;
    @Value("${practice.service.quiz-url:http://repo-quiz}")
    private String quizUrl;

    // -----------------------------------------------------------------------
    // Add module (chọn study set có sẵn)
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public ScheduleModuleResponse addModule(AddModuleToScheduleRequest req) {
        log.info("[ScheduleModule] addModule scheduleId={} order={} type={}",
                req.getScheduleId(), req.getModuleOrder(), req.getModuleType());

        // First-check (trong cùng transaction, có thể thua race → DB unique sẽ bắt)
        if (moduleRepo.existsByScheduleIdAndModuleOrderAndDeletedFalse(
                req.getScheduleId(), req.getModuleOrder())) {
            throw new ApiException(ErrorCode.CONFLICT,
                    "Module với thứ tự " + req.getModuleOrder() + " đã tồn tại trong buổi học này",
                    HttpStatus.CONFLICT);
        }

        ScheduleModule entity = moduleRepo
            .findByScheduleIdAndModuleOrderAndDeletedTrue(req.getScheduleId(), req.getModuleOrder())
            .orElseGet(ScheduleModule::new);

        entity.setScheduleId(req.getScheduleId());
        entity.setModuleType(req.getModuleType());
        entity.setModuleOrder(req.getModuleOrder());
        entity.setTitle(req.getTitle());
        entity.setDescription(req.getDescription());
        entity.setContentSetId(req.getContentSetId());
        entity.setContentFolderId(req.getContentFolderId());
        entity.setExternalRefJson(req.getExternalRefJson());
        entity.setIsRequired(req.getIsRequired() != null ? req.getIsRequired() : true);
        entity.setIsActive(true);
        entity.setDeleted(false);

        try {
            entity = moduleRepo.save(entity);
        } catch (DataIntegrityViolationException ex) {
            // Race condition: 2 request vào cùng lúc, unique constraint bắt
            throw new ApiException(ErrorCode.CONFLICT,
                    "Module với thứ tự " + req.getModuleOrder() + " đã tồn tại (concurrent conflict)",
                    HttpStatus.CONFLICT);
        }

        log.info("[ScheduleModule] Created moduleId={}", entity.getId());
        return toResponse(entity);
    }

    // -----------------------------------------------------------------------
    // Import Excel → tạo content mới → lưu module
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public ScheduleModuleResponse importModule(ImportModuleToScheduleRequest req, MultipartFile file) {
        log.info("[ScheduleModule] importModule scheduleId={} order={} type={}",
                req.getScheduleId(), req.getModuleOrder(), req.getModuleType());

        // First-check duplicate order
        if (moduleRepo.existsByScheduleIdAndModuleOrderAndDeletedFalse(
                req.getScheduleId(), req.getModuleOrder())) {
            throw new ApiException(ErrorCode.CONFLICT,
                    "Module với thứ tự " + req.getModuleOrder() + " đã tồn tại trong buổi học này",
                    HttpStatus.CONFLICT);
        }

        // Gọi repo ngoài để import Excel
        String serviceUrl = resolveServiceUrl(req.getModuleType());
        HierarchicalImportResult result;
        try {
            result = practiceWebClient.importExcel(serviceUrl, file, TypeName.LEARNING);
        } catch (Exception e) {
            log.error("[ScheduleModule] Import failed for type={}: {}", req.getModuleType(), e.getMessage());
            throw new ApiException(ErrorCode.E305,
                    "Không thể kết nối đến dịch vụ ôn luyện: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        }

        // Validate kết quả import
        List<String> studySetIds = result.getStudySetIds();
        if (studySetIds == null || studySetIds.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST,
                    "Import không tạo ra study set nào. Kiểm tra lại file Excel.",
                    HttpStatus.BAD_REQUEST);
        }
        if (studySetIds.size() > 1) {
            throw new ApiException(ErrorCode.CONFLICT,
                    "Import tạo ra " + studySetIds.size() + " study sets — không xác định được set nào để liên kết. "
                            + "Hãy import file Excel chứa đúng 1 study set.",
                    HttpStatus.CONFLICT);
        }

        String contentSetId = studySetIds.get(0);
        log.info("[ScheduleModule] Import OK, contentSetId={}", contentSetId);

        ScheduleModule entity = moduleRepo
            .findByScheduleIdAndModuleOrderAndDeletedTrue(req.getScheduleId(), req.getModuleOrder())
            .orElseGet(ScheduleModule::new);

        entity.setScheduleId(req.getScheduleId());
        entity.setModuleType(req.getModuleType());
        entity.setModuleOrder(req.getModuleOrder());
        entity.setTitle(req.getTitle());
        entity.setDescription(req.getDescription());
        entity.setContentSetId(contentSetId);
        entity.setContentFolderId(null);
        entity.setExternalRefJson(null);
        entity.setIsRequired(req.getIsRequired() != null ? req.getIsRequired() : true);
        entity.setIsActive(true);
        entity.setDeleted(false);

        try {
            entity = moduleRepo.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(ErrorCode.CONFLICT,
                    "Module với thứ tự " + req.getModuleOrder() + " đã tồn tại (concurrent conflict)",
                    HttpStatus.CONFLICT);
        }

        log.info("[ScheduleModule] importModule created moduleId={}", entity.getId());
        return toResponse(entity);
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    @Override
    public ScheduleModuleResponse getById(String moduleId) {
        return toResponse(findById(moduleId));
    }

    @Override
    public List<ScheduleModuleResponse> getByScheduleId(String scheduleId) {
        return moduleRepo
                .findByScheduleIdAndIsActiveTrueAndDeletedFalseOrderByModuleOrderAsc(scheduleId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Soft delete
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void removeModule(String moduleId) {
        log.info("[ScheduleModule] removeModule id={}", moduleId);
        ScheduleModule entity = findById(moduleId);
        entity.setDeleted(true);
        entity.setIsActive(false);
        moduleRepo.save(entity);
        log.info("[ScheduleModule] Soft-deleted moduleId={}", moduleId);
    }

    // -----------------------------------------------------------------------
    // Reorder (pessimistic lock)
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void reorderModules(String scheduleId, ReorderScheduleModulesRequest request) {
        log.info("[ScheduleModule] reorderModules scheduleId={}, {} items",
                scheduleId, request.getItems().size());

        // Pessimistic lock trên toàn bộ module của buổi học
        moduleRepo.findByScheduleIdForUpdate(scheduleId);

        try {
            for (ReorderScheduleModulesRequest.ReorderItem item : request.getItems()) {
                ScheduleModule module = findById(item.getId());
                module.setModuleOrder(item.getNewOrder());
                moduleRepo.save(module);
            }
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(ErrorCode.CONFLICT,
                    "Thứ tự cập nhật bị trùng — kiểm tra lại danh sách reorder.",
                    HttpStatus.CONFLICT);
        }

        log.info("[ScheduleModule] reorderModules done for scheduleId={}", scheduleId);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ScheduleModule findById(String id) {
        return moduleRepo.findById(id)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ApiException(ErrorCode.E227,
                        "Không tìm thấy module với id: " + id, HttpStatus.NOT_FOUND));
    }

    private String resolveServiceUrl(ScheduleModuleType type) {
        return switch (type) {
            case FLASHCARD     -> flashcardUrl;
            case WRITING       -> writingUrl;
            case KANJI         -> kanjiUrl;
            case PRONUNCIATION -> pronunciationUrl;
            case QUIZ          -> quizUrl;
        };
    }

    private ScheduleModuleResponse toResponse(ScheduleModule m) {
        return ScheduleModuleResponse.builder()
                .id(m.getId())
                .scheduleId(m.getScheduleId())
                .moduleType(m.getModuleType())
                .moduleOrder(m.getModuleOrder())
                .title(m.getTitle())
                .description(m.getDescription())
                .contentSetId(m.getContentSetId())
                .contentFolderId(m.getContentFolderId())
                .externalRefJson(m.getExternalRefJson())
                .isRequired(m.getIsRequired())
                .isActive(m.getIsActive())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
