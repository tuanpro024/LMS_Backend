package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import org.springframework.security.core.Authentication;
import com.lms.videocourse.dto.SyllabusTreeDTO;
import com.lms.videocourse.dto.response.SyllabusCourseListDTO;
import com.lms.videocourse.dto.response.SyncRunDTO;
import com.lms.videocourse.entity.SyllabusSyncRun;
import com.lms.videocourse.entity.enums.SyncTriggerType;
import com.lms.videocourse.repository.SyllabusSyncRunRepository;
import com.lms.videocourse.service.ActivityMappingService;
import com.lms.videocourse.service.SyllabusService;
import com.lms.videocourse.service.SyllabusSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/syllabus")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusService syllabusService;
    private final SyllabusSyncService syllabusSyncService;
    private final ActivityMappingService mappingService;
    private final SyllabusSyncRunRepository syncRunRepository;

    /**
     * API to get the full Syllabus hierarchy (backward compatible).
     * Returns: list of packages, each containing its folders, study sets, steps, and activities.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SyllabusTreeDTO>>> getAllSyllabus() {
        List<SyllabusTreeDTO> data = syllabusService.getFullSyllabusTree();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * Get list of all synced CMS courses with summary info.
     */
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<SyllabusCourseListDTO>>> getCourseList() {
        List<SyllabusCourseListDTO> data = syllabusService.getCourseList();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/courses/count")
    public ResponseEntity<ApiResponse<Long>> countActiveCourses() {
        return ResponseEntity.ok(ApiResponse.ok(syllabusService.countActiveCourses()));
    }

    /**
     * Get full syllabus tree for a specific CMS course.
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<SyllabusTreeDTO>> getCourseDetail(
            @PathVariable String courseId) {
        SyllabusTreeDTO data = syllabusService.getCourseDetail(courseId);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * Trigger a manual syllabus sync from CMS.
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<SyncRunDTO>> triggerSync() {
        try {
            SyllabusSyncRun run = syllabusSyncService.syncAll(SyncTriggerType.MANUAL);
            return ResponseEntity.ok(ApiResponse.ok(toSyncRunDTO(run)));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("409", e.getMessage()));
        }
    }

    /**
     * Get sync run history.
     */
    @GetMapping("/sync-runs")
    public ResponseEntity<ApiResponse<List<SyncRunDTO>>> getSyncRuns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SyllabusSyncRun> runs = syncRunRepository.findAllByOrderByStartedAtDesc(PageRequest.of(page, size));
        List<SyncRunDTO> data = runs.getContent().stream()
                .map(this::toSyncRunDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * Generate actual course content from syllabus and mappings.
     */
    @PostMapping("/generate/{cmsCourseId}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<String>> generateCourse(
            @PathVariable String cmsCourseId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        String packageId = mappingService.generateCourse(cmsCourseId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(packageId));
    }

    private SyncRunDTO toSyncRunDTO(SyllabusSyncRun run) {
        return SyncRunDTO.builder()
                .id(run.getId())
                .triggerType(run.getTriggerType().name())
                .status(run.getStatus().name())
                .startedAt(run.getStartedAt())
                .finishedAt(run.getFinishedAt())
                .summary(run.getSummary())
                .errorMessage(run.getErrorMessage())
                .build();
    }
}
