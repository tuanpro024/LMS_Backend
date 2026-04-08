package com.lms.onllearning.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.content.common.entity.TypeName;
import com.lms.onllearning.client.PracticeModuleWebClient;
import com.lms.onllearning.dto.request.AddModuleToScheduleRequest;
import com.lms.onllearning.dto.request.ImportModuleToScheduleRequest;
import com.lms.onllearning.dto.request.ReorderScheduleModulesRequest;
import com.lms.onllearning.dto.request.UpdateMyScheduleModuleProgressRequest;
import com.lms.onllearning.dto.response.CourseStudentModuleProgressResponse;
import com.lms.onllearning.dto.response.ScheduleModuleResponse;
import com.lms.onllearning.dto.response.ScheduleSessionProgressResponse;
import com.lms.onllearning.entity.OnlineCourse;
import com.lms.onllearning.entity.ScheduleModule;
import com.lms.onllearning.entity.ScheduleModuleUserProgress;
import com.lms.onllearning.entity.SyllabusSchedule;
import com.lms.onllearning.entity.enums.ScheduleModuleType;
import com.lms.onllearning.repository.OnlineCourseRepository;
import com.lms.onllearning.repository.ScheduleModuleRepository;
import com.lms.onllearning.repository.ScheduleModuleUserProgressRepository;
import com.lms.onllearning.repository.SyllabusScheduleRepository;
import com.lms.onllearning.repository.TimetableSessionRepository;
import com.lms.onllearning.repository.projection.TimetableSessionView;
import com.lms.onllearning.service.IScheduleModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleModuleServiceImpl implements IScheduleModuleService {

    private final ScheduleModuleRepository moduleRepo;
    private final PracticeModuleWebClient practiceWebClient;
    private final OnlineCourseRepository onlineCourseRepository;
    private final SyllabusScheduleRepository syllabusScheduleRepository;
    private final TimetableSessionRepository timetableSessionRepository;
    private final ScheduleModuleUserProgressRepository userProgressRepository;

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

        validateContentSetMapping(req.getModuleType(), req.getContentSetId());

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

    @Override
    public ScheduleSessionProgressResponse getMySessionProgress(String courseId, String scheduleId, String userId, String email) {
        if (!StringUtils.hasText(scheduleId)) {
            throw new ApiException(ErrorCode.BAD_REQUEST,
                "scheduleId là bắt buộc.",
                HttpStatus.BAD_REQUEST);
        }

        SyllabusSchedule currentSchedule = syllabusScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ApiException(
                ErrorCode.E227,
                "Không tìm thấy buổi học với id: " + scheduleId,
                HttpStatus.NOT_FOUND));

        String scheduleSyllabusId = currentSchedule.getSyllabus() != null ? currentSchedule.getSyllabus().getId() : null;
        if (!StringUtils.hasText(scheduleSyllabusId)) {
            throw new ApiException(
                ErrorCode.BAD_REQUEST,
                "Buổi học chưa liên kết syllabus.",
                HttpStatus.BAD_REQUEST);
        }

        OnlineCourse course = resolveCourseForProgress(courseId, scheduleSyllabusId);

        List<ScheduleModule> modules = moduleRepo
            .findByScheduleIdAndIsActiveTrueAndDeletedFalseOrderByModuleOrderAsc(scheduleId);

        String userKey = resolveUserKey(userId, email);
        Map<String, ScheduleModuleUserProgress> localProgressByModule = loadLocalProgressByModule(modules, userKey);

        List<ScheduleSessionProgressResponse.ModuleProgressItem> moduleProgresses = modules.stream()
            .map(module -> resolveModuleProgress(module, localProgressByModule.get(module.getId())))
            .toList();

        ScheduleSessionProgressResponse.HomeworkSummary homeworkSummary = buildHomeworkSummary(modules, moduleProgresses);
        ScheduleSessionProgressResponse.ProgressLock progressLock = resolveProgressLock(
            course.getSyllabusId(),
            currentSchedule,
            email);

        return new ScheduleSessionProgressResponse(
            new ScheduleSessionProgressResponse.CourseInfo(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getLevel(),
                course.getSyllabusId(),
                course.getPrice(),
                course.getRating(),
                course.getThumbnail()),
            new ScheduleSessionProgressResponse.SessionInfo(
                currentSchedule.getId(),
                currentSchedule.getSessionNo(),
                currentSchedule.getTopic(),
                currentSchedule.getContent()),
            moduleProgresses,
            homeworkSummary,
            progressLock);
    }

    private OnlineCourse resolveCourseForProgress(String courseId, String scheduleSyllabusId) {
        if (StringUtils.hasText(courseId)) {
            OnlineCourse course = onlineCourseRepository.findByIdAndDeletedFalse(courseId.trim())
                .orElseThrow(() -> new ApiException(
                    ErrorCode.E227,
                    "Không tìm thấy khóa học: " + courseId,
                    HttpStatus.NOT_FOUND));

            if (!StringUtils.hasText(course.getSyllabusId())) {
                throw new ApiException(
                    ErrorCode.BAD_REQUEST,
                    "Khóa học chưa liên kết syllabus.",
                    HttpStatus.BAD_REQUEST);
            }

            if (!course.getSyllabusId().equals(scheduleSyllabusId)) {
                throw new ApiException(
                    ErrorCode.BAD_REQUEST,
                    "Buổi học không thuộc syllabus của khóa học.",
                    HttpStatus.BAD_REQUEST);
            }
            return course;
        }

        return onlineCourseRepository.findFirstBySyllabusIdAndDeletedFalseOrderByCreatedAtDesc(scheduleSyllabusId)
            .orElseThrow(() -> new ApiException(
                ErrorCode.E227,
                "Không tìm thấy khóa học gắn với syllabus của buổi học.",
                HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public void updateMyModuleProgress(String moduleId, String userId, String email,
            UpdateMyScheduleModuleProgressRequest request) {
        ScheduleModule module = findById(moduleId);
        String userKey = resolveUserKey(userId, email);
        if (!StringUtils.hasText(userKey)) {
            throw new ApiException(ErrorCode.BAD_REQUEST,
                    "Không xác định được người dùng để cập nhật progress.",
                    HttpStatus.UNAUTHORIZED);
        }

        ScheduleModuleUserProgress progress = userProgressRepository
                .findByModuleIdAndUserIdAndDeletedFalse(module.getId(), userKey)
                .orElseGet(() -> ScheduleModuleUserProgress.builder()
                        .moduleId(module.getId())
                        .userId(userKey)
                        .build());

        double percentage = request.getProgressPercentage() == null ? 0.0 : clampAndRound(request.getProgressPercentage());
        boolean completed = Boolean.TRUE.equals(request.getCompleted()) || percentage >= 100.0;

        progress.setUserEmail(StringUtils.hasText(email) ? email.trim().toLowerCase(Locale.ROOT) : progress.getUserEmail());
        progress.setProgressPercentage(percentage);
        progress.setCompleted(completed);
        progress.setCompletedItems(request.getCompletedItems());
        progress.setTotalItems(request.getTotalItems());
        progress.setLastInteractedAt(Instant.now());
        progress.setCompletedAt(completed ? Instant.now() : null);
        progress.setDeleted(false);

        userProgressRepository.save(progress);
    }

    @Override
    public List<CourseStudentModuleProgressResponse> getAllStudentProgress() {
        log.info("[ScheduleModule] getAllStudentProgress");

        // Get all progress records (no filtering by course)
        List<ScheduleModuleUserProgress> allProgresses = userProgressRepository.findByDeletedFalse();
        if (allProgresses.isEmpty()) {
            return List.of();
        }

        // Group progresses by Module ID to batch fetch module info
        Map<String, List<ScheduleModuleUserProgress>> progressesByModuleId = allProgresses.stream()
            .collect(Collectors.groupingBy(ScheduleModuleUserProgress::getModuleId));

        List<String> moduleIds = new ArrayList<>(progressesByModuleId.keySet());
        List<ScheduleModule> modules = moduleRepo.findAllById(moduleIds);

        Map<String, ScheduleModule> moduleById = modules.stream()
            .collect(Collectors.toMap(ScheduleModule::getId, m -> m, (a, b) -> a));

        // Get all schedules and courses
        List<String> scheduleIds = modules.stream()
            .map(ScheduleModule::getScheduleId)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();

        List<SyllabusSchedule> schedules = syllabusScheduleRepository.findAllById(scheduleIds);
        Map<String, SyllabusSchedule> scheduleById = schedules.stream()
            .collect(Collectors.toMap(SyllabusSchedule::getId, s -> s, (a, b) -> a));

        // Build responses per course (map schedule ID to course)
        Map<String, OnlineCourse> courseByScheduleId = new HashMap<>();

        for (SyllabusSchedule schedule : schedules) {
            if (schedule.getSyllabus() == null || !StringUtils.hasText(schedule.getSyllabus().getId())) {
                continue;
            }
            String syllabusId = schedule.getSyllabus().getId();

            // Find or create course for this syllabus
            OnlineCourse course = onlineCourseRepository
                .findFirstBySyllabusIdAndDeletedFalseOrderByCreatedAtDesc(syllabusId)
                .orElse(null);
            if (course == null) continue;

            courseByScheduleId.put(schedule.getId(), course);
        }

        // Build responses per course
        Map<String, List<CourseStudentModuleProgressResponse.StudentModuleProgressItem>> itemsByCourseId = new HashMap<>();
        Map<String, CourseInfo> courseInfo = new HashMap<>();

        for (Map.Entry<String, List<ScheduleModuleUserProgress>> entry : progressesByModuleId.entrySet()) {
            String moduleId = entry.getKey();
            List<ScheduleModuleUserProgress> moduleProgresses = entry.getValue();

            ScheduleModule module = moduleById.get(moduleId);
            if (module == null) continue;

            SyllabusSchedule schedule = scheduleById.get(module.getScheduleId());
            if (schedule == null) continue;

            OnlineCourse course = courseByScheduleId.get(schedule.getId());
            if (course == null) continue;

            String courseId = course.getId();
            courseInfo.putIfAbsent(courseId, new CourseInfo(course.getId(), course.getCode(), course.getName(), course.getSyllabusId()));
            Integer sessionNo = schedule.getSessionNo();

            for (ScheduleModuleUserProgress progress : moduleProgresses) {
                String fallbackEmail = StringUtils.hasText(progress.getUserId()) && progress.getUserId().contains("@")
                    ? progress.getUserId()
                    : null;

                CourseStudentModuleProgressResponse.StudentModuleProgressItem item = 
                    new CourseStudentModuleProgressResponse.StudentModuleProgressItem(
                        module.getId(),
                        module.getScheduleId(),
                        sessionNo,
                        module.getModuleOrder(),
                        module.getTitle(),
                        module.getModuleType(),
                        progress.getUserId(),
                        StringUtils.hasText(progress.getUserEmail()) ? progress.getUserEmail() : fallbackEmail,
                        clampAndRound(progress.getProgressPercentage() == null ? 0.0 : progress.getProgressPercentage()),
                        Boolean.TRUE.equals(progress.getCompleted()),
                        progress.getCompletedItems(),
                        progress.getTotalItems(),
                        progress.getLastInteractedAt(),
                        progress.getCompletedAt());

                itemsByCourseId.computeIfAbsent(courseId, k -> new ArrayList<>()).add(item);
            }
        }

        // Convert to response list
        return itemsByCourseId.entrySet().stream()
            .map(entry -> {
                String courseId = entry.getKey();
                List<CourseStudentModuleProgressResponse.StudentModuleProgressItem> items = entry.getValue();
                CourseInfo info = courseInfo.get(courseId);

                long uniqueModuleCount = items.stream()
                    .map(CourseStudentModuleProgressResponse.StudentModuleProgressItem::moduleId)
                    .distinct()
                    .count();

                return new CourseStudentModuleProgressResponse(
                    info.courseId,
                    info.code,
                    info.name,
                    info.syllabusId,
                    (int) uniqueModuleCount,
                    items.size(),
                    items);
            })
            .toList();
    }

    private static class CourseInfo {
        String courseId;
        String code;
        String name;
        String syllabusId;

        CourseInfo(String courseId, String code, String name, String syllabusId) {
            this.courseId = courseId;
            this.code = code;
            this.name = name;
            this.syllabusId = syllabusId;
        }
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
            case FLASHCARD -> flashcardUrl;
            case WRITING -> writingUrl;
            case KANJI -> kanjiUrl;
            case PRONUNCIATION -> pronunciationUrl;
            case QUIZ -> quizUrl;
        };
    }

    private void validateContentSetMapping(ScheduleModuleType moduleType, String contentSetId) {
        if (moduleType == null || contentSetId == null || contentSetId.isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST,
                    "moduleType/contentSetId không hợp lệ.",
                    HttpStatus.BAD_REQUEST);
        }

        String serviceUrl = resolveServiceUrl(moduleType);
        try {
            StudySetResponse studySet = practiceWebClient.getStudySetById(serviceUrl, contentSetId);
            if (studySet == null) {
                throw new ApiException(ErrorCode.BAD_REQUEST,
                        "Content set " + contentSetId + " không tồn tại cho moduleType " + moduleType,
                        HttpStatus.BAD_REQUEST);
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.E305,
                    "Không thể kiểm tra content set từ dịch vụ ôn luyện: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        }
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

        private ScheduleSessionProgressResponse.ModuleProgressItem resolveModuleProgress(
            ScheduleModule module,
            ScheduleModuleUserProgress localProgress) {
        Double localPercentage = localProgress != null ? clampAndRound(localProgress.getProgressPercentage()) : null;
        boolean localCompleted = localProgress != null
            && (Boolean.TRUE.equals(localProgress.getCompleted())
                || (localPercentage != null && localPercentage >= 100.0));

        if (!StringUtils.hasText(module.getContentSetId())) {
            double fallback = localCompleted ? 100.0 : (localPercentage != null ? localPercentage : 0.0);
            return new ScheduleSessionProgressResponse.ModuleProgressItem(
                    module.getId(),
                    module.getModuleOrder(),
                    module.getTitle(),
                    module.getModuleType(),
                    module.getContentSetId(),
                fallback,
                localCompleted || fallback >= 100.0,
                localProgress != null ? "ONLINE_COURSE" : "NONE",
                localProgress != null ? null : "Module chưa có contentSetId.");
        }

        Double progress = switch (module.getModuleType()) {
            case FLASHCARD -> practiceWebClient.getFlashcardProgressPercentage(flashcardUrl, module.getContentSetId());
            case WRITING -> practiceWebClient.getWritingProgressPercentage(writingUrl, module.getContentSetId());
            case KANJI -> practiceWebClient.getKanjiProgressPercentage(kanjiUrl, module.getContentSetId());
            case PRONUNCIATION -> practiceWebClient.getPronunciationProgressPercentage(pronunciationUrl,
                    module.getContentSetId());
            case QUIZ -> practiceWebClient.getQuizStudySetProgressPercentage(quizUrl, module.getContentSetId());
        };

        if (progress == null && localProgress == null) {
            return new ScheduleSessionProgressResponse.ModuleProgressItem(
                    module.getId(),
                    module.getModuleOrder(),
                    module.getTitle(),
                    module.getModuleType(),
                    module.getContentSetId(),
                    0.0,
                    false,
                    "UNAVAILABLE",
                    "Không lấy được progress từ dịch vụ module.");
        }

        double liveNormalized = progress == null ? 0.0 : clampAndRound(progress);
        double localNormalized = localPercentage == null ? 0.0 : localPercentage;
        double normalized = Math.max(liveNormalized, localNormalized);
        boolean completed = localCompleted || normalized >= 100.0;
        String source = localProgress != null
                ? (progress == null ? "ONLINE_COURSE" : "LIVE+ONLINE_COURSE")
                : "LIVE";

        return new ScheduleSessionProgressResponse.ModuleProgressItem(
                module.getId(),
                module.getModuleOrder(),
                module.getTitle(),
                module.getModuleType(),
                module.getContentSetId(),
                normalized,
                completed,
                source,
                null);
    }

    private Map<String, ScheduleModuleUserProgress> loadLocalProgressByModule(List<ScheduleModule> modules, String userKey) {
        if (!StringUtils.hasText(userKey) || modules == null || modules.isEmpty()) {
            return Map.of();
        }

        List<String> moduleIds = modules.stream()
                .map(ScheduleModule::getId)
                .filter(StringUtils::hasText)
                .toList();

        if (moduleIds.isEmpty()) {
            return Map.of();
        }

        Map<String, ScheduleModuleUserProgress> map = new HashMap<>();
        userProgressRepository.findByModuleIdInAndUserIdAndDeletedFalse(moduleIds, userKey)
                .forEach(item -> map.put(item.getModuleId(), item));
        return map;
    }

    private String resolveUserKey(String userId, String email) {
        if (StringUtils.hasText(userId)) {
            return userId.trim();
        }
        if (StringUtils.hasText(email)) {
            return email.trim().toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private ScheduleSessionProgressResponse.HomeworkSummary buildHomeworkSummary(
            List<ScheduleModule> modules,
            List<ScheduleSessionProgressResponse.ModuleProgressItem> progresses) {

        List<ScheduleSessionProgressResponse.ModuleProgressItem> requiredProgresses = progresses;
        if (modules.stream().anyMatch(m -> Boolean.TRUE.equals(m.getIsRequired()))) {
            requiredProgresses = progresses.stream()
                    .filter(item -> modules.stream().anyMatch(
                            m -> m.getId().equals(item.moduleId()) && Boolean.TRUE.equals(m.getIsRequired())))
                    .toList();
        }

        int requiredCount = requiredProgresses.size();
        double completedSum = requiredProgresses.stream()
                .map(ScheduleSessionProgressResponse.ModuleProgressItem::progressPercentage)
                .filter(p -> p != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        double total = requiredCount * 100.0;
        double completionPercent = total <= 0 ? 0.0 : (completedSum * 100.0 / total);
        double roundedCompletedSum = clampAndRound(completedSum);
        double roundedTotal = clampAndRound(total);
        double roundedCompletion = clampAndRound(completionPercent);

        String text = String.format(Locale.ROOT, "%.2f / %.2f", roundedCompletedSum, roundedTotal);
        return new ScheduleSessionProgressResponse.HomeworkSummary(
                requiredCount,
                roundedCompletedSum,
                roundedTotal,
                roundedCompletion,
                text);
    }

    private ScheduleSessionProgressResponse.ProgressLock resolveProgressLock(
            String syllabusId,
            SyllabusSchedule currentSchedule,
            String email) {

        Integer currentSessionNo = currentSchedule.getSessionNo();
        if (currentSessionNo == null) {
            return new ScheduleSessionProgressResponse.ProgressLock(
                    false,
                    null,
                    null,
                    null,
                    "Buổi học hiện tại chưa có sessionNo.");
        }

        Optional<SyllabusSchedule> nextScheduleOpt = syllabusScheduleRepository
                .findFirstBySyllabus_IdAndSessionNoGreaterThanOrderBySessionNoAsc(syllabusId, currentSessionNo);

        if (nextScheduleOpt.isEmpty()) {
            return new ScheduleSessionProgressResponse.ProgressLock(
                    false,
                    null,
                    null,
                    null,
                    "Không có buổi học tiếp theo trong schedule.");
        }

        SyllabusSchedule nextSchedule = nextScheduleOpt.get();
        Instant nextSessionStartAt = resolveNextSessionStart(nextSchedule.getId(), email);
        if (nextSessionStartAt == null) {
            return new ScheduleSessionProgressResponse.ProgressLock(
                    false,
                    nextSchedule.getId(),
                    nextSchedule.getSessionNo(),
                    null,
                    "Chưa có dữ liệu thời gian buổi kế tiếp từ timetable.");
        }

        boolean locked = !Instant.now().isBefore(nextSessionStartAt);
        String reason = locked
                ? "Đã đến thời điểm buổi học kế tiếp, progress buổi hiện tại bị khóa."
                : "Chưa tới buổi học kế tiếp, progress vẫn mở.";

        return new ScheduleSessionProgressResponse.ProgressLock(
                locked,
                nextSchedule.getId(),
                nextSchedule.getSessionNo(),
                nextSessionStartAt,
                reason);
    }

    private Instant resolveNextSessionStart(String nextScheduleId, String email) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(nextScheduleId)) {
            return null;
        }

        return timetableSessionRepository.findTimetableByParticipantEmail(email.trim().toLowerCase(Locale.ROOT))
                .stream()
                .filter(item -> nextScheduleId.equals(item.getSyllabusScheduleId()))
                .map(this::toSessionStartInstant)
                .filter(java.util.Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(null);
    }

    private Instant toSessionStartInstant(TimetableSessionView view) {
        String dateText = firstNonBlank(view.getSessionDate(), view.getDate());
        if (!StringUtils.hasText(dateText)) {
            return null;
        }

        Instant parsedDateTime = parseToInstant(dateText);
        if (parsedDateTime != null) {
            return parsedDateTime;
        }

        LocalDate localDate = parseLocalDate(dateText);
        if (localDate == null) {
            return null;
        }

        LocalTime start = parseLocalTime(view.getStartTime());
        if (start == null) {
            start = LocalTime.MIN;
        }

        return LocalDateTime.of(localDate, start)
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }

    private Instant parseToInstant(String text) {
        try {
            return Instant.parse(text);
        } catch (Exception ignored) {
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (Exception ignored) {
        }
        try {
            return ZonedDateTime.parse(text).toInstant();
        } catch (Exception ignored) {
        }
        return null;
    }

    private LocalDate parseLocalDate(String text) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private LocalTime parseLocalTime(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String normalized = text.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm:ss"));

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(normalized, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        if (StringUtils.hasText(second)) {
            return second.trim();
        }
        return null;
    }

    private double clampAndRound(double value) {
        double v = value;
        if (v < 0) {
            v = 0;
        }
        return Math.round(v * 100.0d) / 100.0d;
    }
}
