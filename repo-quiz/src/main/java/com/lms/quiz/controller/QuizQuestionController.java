package com.lms.quiz.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.quiz.dto.request.CreateQuestionRequest;
import com.lms.quiz.dto.response.QuestionResponse;
import com.lms.quiz.service.IQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuizQuestionController {

    private final IQuestionService questionService;

    @PostMapping("/{quizId}")
    @RequiresTicket(module = TicketModuleEnum.QUIZ)
    public ResponseEntity<ApiResponse<QuestionResponse>> addQuestion(
            @PathVariable String quizId,
            @Valid @RequestBody CreateQuestionRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(questionService.addQuestion(quizId, request, userId)));
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByQuiz(
            @PathVariable String quizId) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.getQuestionsByQuizId(quizId)));
    }

    @PutMapping("/{questionId}")
    @RequiresTicket(module = TicketModuleEnum.QUIZ)
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable String questionId,
            @Valid @RequestBody CreateQuestionRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(ApiResponse.ok(questionService.updateQuestion(questionId, request, userId)));
    }

    @DeleteMapping("/{questionId}")
    @RequiresTicket(module = TicketModuleEnum.QUIZ)
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable String questionId,
            Authentication authentication) {
        questionService.deleteQuestion(questionId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
