package com.lms.content.common.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.dto.request.CreateSlotRequest;
import com.lms.content.common.dto.request.UpdateSlotRequest;
import com.lms.content.common.dto.response.SlotResponse;
import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.Slot;
import com.lms.content.common.entity.Subject;
import com.lms.content.common.mapper.SlotMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.SlotRepository;
import com.lms.content.common.repository.SubjectRepository;
import com.lms.content.common.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SlotServiceImpl implements SlotService {

    private final SlotRepository slotRepository;
    private final SubjectRepository subjectRepository;
    private final FolderRepository folderRepository;
    private final SlotMapper slotMapper;

    @Override
    public SlotResponse createSlot(CreateSlotRequest request, String userId) {
        log.info("Creating slot for user: {}", userId);

        // Hook: validate
        validateCreateSlot(request, userId);

        // Verify subject exists and user has access
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

        if (!subject.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add slot to this subject");
        }

        Slot slot = slotMapper.toEntity(request);
        slot.setUserId(userId);
        slot.setSubject(subject);

        // Hook: before save
        beforeSaveSlot(slot, request);

        Slot saved = slotRepository.save(slot);

        // Hook: after save
        afterSaveSlot(saved);

        return slotMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SlotResponse getSlotById(String id) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));
        return slotMapper.toResponse(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsBySubjectId(String subjectId) {
        List<Slot> slots = slotRepository.findBySubjectId(subjectId);
        return slotMapper.toResponseList(slots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getAllSlots() {
        List<Slot> slots = slotRepository.findAll();
        return slotMapper.toResponseList(slots);
    }

    @Override
    public SlotResponse updateSlot(String id, UpdateSlotRequest request, String userId) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        if (!slot.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this slot");
        }

        // Hook: validate update
        validateUpdateSlot(slot, request, userId);

        if (request.getName() != null) {
            slot.setName(request.getName());
        }
        if (request.getSlotNumber() != null) {
            slot.setSlotNumber(request.getSlotNumber());
        }
        if (request.getDescription() != null) {
            slot.setDescription(request.getDescription());
        }

        // Hook: before update
        beforeUpdateSlot(slot, request);

        Slot updated = slotRepository.save(slot);

        // Hook: after update
        afterUpdateSlot(updated);

        return slotMapper.toResponse(updated);
    }

    @Override
    public void deleteSlot(String id, String userId) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        if (!slot.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this slot");
        }

        // Hook: before delete
        beforeDeleteSlot(slot, userId);

        slotRepository.delete(slot);

        // Hook: after delete
        afterDeleteSlot(id, userId);
    }

    @Override
    public SlotResponse addFolderToSlot(String slotId, String folderId, String userId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        if (!slot.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this slot");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add this folder");
        }

        slot.addFolder(folder);
        Slot updated = slotRepository.save(slot);

        return slotMapper.toResponse(updated);
    }

    @Override
    public SlotResponse removeFolderFromSlot(String slotId, String folderId, String userId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        if (!slot.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this slot");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        slot.removeFolder(folder);
        Slot updated = slotRepository.save(slot);

        return slotMapper.toResponse(updated);
    }

    // ========== EXTENSION HOOKS ==========

    protected void validateCreateSlot(CreateSlotRequest request, String userId) {
    }

    protected void beforeSaveSlot(Slot slot, CreateSlotRequest request) {
    }

    protected void afterSaveSlot(Slot saved) {
    }

    protected void validateUpdateSlot(Slot entity, UpdateSlotRequest request, String userId) {
    }

    protected void beforeUpdateSlot(Slot entity, UpdateSlotRequest request) {
    }

    protected void afterUpdateSlot(Slot updated) {
    }

    protected void beforeDeleteSlot(Slot entity, String userId) {
    }

    protected void afterDeleteSlot(String slotId, String userId) {
    }
}
