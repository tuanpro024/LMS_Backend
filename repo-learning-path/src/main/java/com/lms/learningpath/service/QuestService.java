package com.lms.learningpath.service;

import com.lms.learningpath.dto.request.CreateQuestRequest;
import com.lms.learningpath.dto.response.QuestProgressInfo;
import com.lms.learningpath.dto.response.QuestResponse;

import java.util.List;

public interface QuestService {

    /**
     * Tạo quest mới (admin)
     */
    QuestResponse createQuest(CreateQuestRequest request);

    /**
     * Lấy danh sách quest đang active của user
     */
    List<QuestResponse> getActiveQuests(String userId);

    /**
     * Cập nhật tiến độ quest sau khi hoàn thành module
     */
    List<QuestProgressInfo> updateQuestProgress(String userId, String moduleId, String studySetId);

    /**
     * Claim thưởng quest
     */
    QuestResponse claimQuest(String userId, String questId, String period);
}