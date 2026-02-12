package com.lms.quiz.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.quiz.dto.request.SubmitQuizRequest;
import com.lms.quiz.dto.response.QuizResultResponse;
import com.lms.quiz.service.IQuizAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final IQuizAttemptService quizAttemptService;

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizResultResponse>> submitQuiz(
            @Valid @RequestBody SubmitQuizRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(ApiResponse.ok(quizAttemptService.submitQuiz(request, userId)));
    }
}
