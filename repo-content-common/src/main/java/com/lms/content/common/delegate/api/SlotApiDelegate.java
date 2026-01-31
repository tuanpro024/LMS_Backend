package com.lms.content.common.delegate.api;

import com.lms.content.common.dto.request.CreateSlotRequest;
import com.lms.content.common.dto.request.UpdateSlotRequest;
import com.lms.content.common.dto.response.SlotResponse;
import com.lms.content.common.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SlotApiDelegate {

    private final SlotService slotService;

    public SlotResponse createSlot(CreateSlotRequest request, String userId) {
        return slotService.createSlot(request, userId);
    }

    public SlotResponse getSlotById(String id) {
        return slotService.getSlotById(id);
    }

    public List<SlotResponse> getSlotsBySubjectId(String subjectId) {
        return slotService.getSlotsBySubjectId(subjectId);
    }

    public List<SlotResponse> getAllSlots() {
        return slotService.getAllSlots();
    }

    public SlotResponse updateSlot(String id, UpdateSlotRequest request, String userId) {
        return slotService.updateSlot(id, request, userId);
    }

    public void deleteSlot(String id, String userId) {
        slotService.deleteSlot(id, userId);
    }

    public SlotResponse addFolderToSlot(String slotId, String folderId, String userId) {
        return slotService.addFolderToSlot(slotId, folderId, userId);
    }

    public SlotResponse removeFolderFromSlot(String slotId, String folderId, String userId) {
        return slotService.removeFolderFromSlot(slotId, folderId, userId);
    }
}
