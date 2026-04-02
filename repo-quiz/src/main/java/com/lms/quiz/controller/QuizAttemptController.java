package com.lms.quiz.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.quiz.dto.request.CheckQuestionRequest;
import com.lms.quiz.dto.request.SubmitQuizRequest;
import com.lms.quiz.dto.request.UpdateQuestionResultRequest;
import com.lms.quiz.dto.response.CheckQuestionResponse;
import com.lms.quiz.dto.response.QuizProgressResponse;
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

    private String resolveUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            return principal.userId();
        }
        return authentication != null ? authentication.getName() : null;
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizResultResponse>> submitQuiz(
            @Valid @RequestBody SubmitQuizRequest request,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(quizAttemptService.submitQuiz(request, userId)));
    }

    @PostMapping("/check-question")
    public ResponseEntity<ApiResponse<CheckQuestionResponse>> checkQuestion(
            @Valid @RequestBody CheckQuestionRequest request,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(quizAttemptService.checkQuestion(request, userId)));
    }

    @PostMapping("/update-question-result")
    public ResponseEntity<ApiResponse<QuizResultResponse>> updateQuestionResult(
            @Valid @RequestBody UpdateQuestionResultRequest request,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(quizAttemptService.updateQuestionResult(request, userId)));
    }

    @GetMapping("/quizzes/{quizId}/progress")
    public ResponseEntity<ApiResponse<QuizProgressResponse>> getQuizProgress(
            @PathVariable String quizId,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(quizAttemptService.getQuizProgress(quizId, userId)));
    }
}
