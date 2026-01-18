package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.CreateSlotRequest;
import com.lms.flashcard.dto.request.UpdateSlotRequest;
import com.lms.flashcard.dto.response.SlotResponse;
import com.lms.flashcard.entity.Folder;
import com.lms.flashcard.entity.Slot;
import com.lms.flashcard.entity.Subject;
import com.lms.flashcard.mapper.SlotMapper;
import com.lms.flashcard.repository.FolderRepository;
import com.lms.flashcard.repository.SlotRepository;
import com.lms.flashcard.repository.SubjectRepository;
import com.lms.flashcard.service.SlotService;
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
        log.info("Creating slot for user: {} with name: {}", userId, request.getName());

        // Verify subject exists and user has access
        Subject subject = subjectRepository.findByIdAndUserId(request.getSubjectId(), userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Subject not found"));

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
        Slot slot = slotRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        // Update fields if provided
        if (request.getName() != null) {
            slot.setName(request.getName());
        }
        if (request.getSlotNumber() != null) {
            slot.setSlotNumber(request.getSlotNumber());
        }
        if (request.getDescription() != null) {
            slot.setDescription(request.getDescription());
        }

        Slot updated = slotRepository.save(slot);
        return slotMapper.toResponse(updated);
    }

    @Override
    public void deleteSlot(String id, String userId) {
        Slot slot = slotRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        // Remove slot association from all folders before deletion
        for (Folder folder : slot.getFolders()) {
            folder.setSlot(null);
        }

        slotRepository.delete(slot);
    }

    @Override
    public SlotResponse addFolderToSlot(String slotId, String folderId, String userId) {
        Slot slot = slotRepository.findByIdAndUserId(slotId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        // Check ownership of folder
        if (!folder.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to add this folder");
        }

        slot.addFolder(folder);
        Slot updated = slotRepository.save(slot);

        return slotMapper.toResponse(updated);
    }

    @Override
    public SlotResponse removeFolderFromSlot(String slotId, String folderId, String userId) {
        Slot slot = slotRepository.findByIdAndUserId(slotId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Slot not found"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Folder not found"));

        slot.removeFolder(folder);
        Slot updated = slotRepository.save(slot);

        return slotMapper.toResponse(updated);
    }
}
