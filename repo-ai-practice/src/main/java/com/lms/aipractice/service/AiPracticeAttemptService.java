package com.lms.aipractice.service;

import com.lms.aipractice.dto.request.CreateAttemptRequest;
import com.lms.aipractice.dto.request.SubmitAnswerRequest;
import com.lms.aipractice.dto.response.AttemptResponse;
import com.lms.aipractice.dto.response.GradingJobResponse;
import com.lms.aipractice.dto.response.GradingResultResponse;

import java.util.List;

public interface AiPracticeAttemptService {
    AttemptResponse createAttempt(CreateAttemptRequest request, String userId);

    AttemptResponse submitAnswer(String attemptId, SubmitAnswerRequest request, String userId);

    AttemptResponse submitAttempt(String attemptId, String userId);

    AttemptResponse getAttempt(String attemptId, String userId);

    List<AttemptResponse> getLatestAttemptsByStudySetIds(List<String> studySetIds, String userId);

    List<GradingResultResponse> getResults(String attemptId, String userId);

    GradingJobResponse getJob(String jobId);
}
