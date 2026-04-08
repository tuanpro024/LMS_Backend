package com.lms.quiz.controller;

import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.common.dto.ApiResponse;
import com.lms.quiz.dto.request.CreateQuizRequest;
import com.lms.quiz.dto.response.QuizDetailResponse;
import com.lms.quiz.dto.response.QuizResponse;
import com.lms.quiz.service.IQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final IQuizService quizService;

    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.QUIZ)
    public ResponseEntity<ApiResponse<QuizDetailResponse>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(quizService.createQuiz(request, userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> getQuiz(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(quizService.getQuizById(id)));
    }

    @GetMapping("/study-set/{studySetId}")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getByStudySet(
            @PathVariable String studySetId) {
        return ResponseEntity.ok(ApiResponse.ok(quizService.getQuizzesByStudySetId(studySetId)));
    }

    @GetMapping("/{id}/attempt")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> getQuizForAttempt(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(quizService.getQuizForAttempt(id)));
    }

    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.QUIZ)
    public ResponseEntity<ApiResponse<QuizDetailResponse>> updateQuiz(
            @PathVariable String id,
            @Valid @RequestBody CreateQuizRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(ApiResponse.ok(quizService.updateQuiz(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.QUIZ)
    public ResponseEntity<Void> deleteQuiz(@PathVariable String id, Authentication authentication) {
        quizService.deleteQuiz(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
