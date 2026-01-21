package com.lms.writing.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.writing.dto.request.CreateSlotRequest;
import com.lms.writing.dto.request.UpdateSlotRequest;
import com.lms.writing.dto.response.SlotResponse;
import com.lms.writing.entity.Folder;
import com.lms.writing.entity.Slot;
import com.lms.writing.entity.Subject;
import com.lms.writing.mapper.SlotMapper;
import com.lms.writing.repository.FolderRepository;
import com.lms.writing.repository.SlotRepository;
import com.lms.writing.repository.SubjectRepository;
import com.lms.writing.service.SlotService;
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

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

        // Check ownership
        if (!subject.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add slot to this subject");
        }

        Slot slot = slotMapper.toEntity(request);
        slot.setUserId(userId);
        slot.setSubject(subject);

        Slot saved = slotRepository.save(slot);
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
    public List<SlotResponse> getAllSlots() {
        List<Slot> slots = slotRepository.findAll();
        return slotMapper.toResponseList(slots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsBySubjectId(String subjectId) {
        List<Slot> slots = slotRepository.findBySubjectId(subjectId);
        return slotMapper.toResponseList(slots);
    }

    @Override
    public SlotResponse updateSlot(String id, UpdateSlotRequest request, String userId) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        // Check ownership
        if (!slot.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this slot");
        }

        slotMapper.updateEntity(slot, request);

        Slot updated = slotRepository.save(slot);
        return slotMapper.toResponse(updated);
    }

    @Override
    public void deleteSlot(String id, String userId) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        // Check ownership
        if (!slot.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this slot");
        }

        slotRepository.delete(slot);
    }

    @Override
    public SlotResponse addFolderToSlot(String slotId, String folderId, String userId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        // Check ownership
        if (!slot.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this slot");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check folder ownership
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

        // Check ownership
        if (!slot.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this slot");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        slot.removeFolder(folder);
        Slot updated = slotRepository.save(slot);
        return slotMapper.toResponse(updated);
    }
}
