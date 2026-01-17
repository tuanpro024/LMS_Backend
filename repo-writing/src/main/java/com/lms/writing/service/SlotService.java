package com.lms.writing.service;

import com.lms.writing.dto.request.CreateSlotRequest;
import com.lms.writing.dto.request.UpdateSlotRequest;
import com.lms.writing.dto.response.SlotResponse;

import java.util.List;

public interface SlotService {

    SlotResponse createSlot(CreateSlotRequest request, String userId);

    SlotResponse getSlotById(String id);

    List<SlotResponse> getSlotsBySubjectId(String subjectId);

    SlotResponse updateSlot(String id, UpdateSlotRequest request, String userId);

    void deleteSlot(String id, String userId);
}
