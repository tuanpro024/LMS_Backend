package com.lms.flashcard.service;

import com.lms.flashcard.dto.request.CreateSlotRequest;
import com.lms.flashcard.dto.request.UpdateSlotRequest;
import com.lms.flashcard.dto.response.SlotResponse;

import java.util.List;

public interface SlotService {

    SlotResponse createSlot(CreateSlotRequest request, String userId);

    SlotResponse getSlotById(String id, String userId);

    List<SlotResponse> getSlotsBySubjectId(String subjectId, String userId);

    List<SlotResponse> getAllSlots(String userId);

    SlotResponse updateSlot(String id, UpdateSlotRequest request, String userId);

    void deleteSlot(String id, String userId);

    SlotResponse addFolderToSlot(String slotId, String folderId, String userId);

    SlotResponse removeFolderFromSlot(String slotId, String folderId, String userId);
}
