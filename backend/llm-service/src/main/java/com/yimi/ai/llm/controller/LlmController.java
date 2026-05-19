package com.yimi.ai.llm.controller;

import com.yimi.ai.llm.dto.*;
import com.yimi.ai.llm.service.ClaudeService;
import com.yimi.ai.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final ClaudeService claudeService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = claudeService.chat(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/skills/generate")
    public ResponseEntity<ApiResponse<SkillGenerateResponse>> generateSkill(
            @Valid @RequestBody SkillGenerateRequest request) {
        SkillGenerateResponse response = claudeService.generateSkill(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/workflows/generate")
    public ResponseEntity<ApiResponse<WorkflowGenerateResponse>> generateWorkflow(
            @Valid @RequestBody WorkflowGenerateRequest request) {
        WorkflowGenerateResponse response = claudeService.generateWorkflow(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/knowledge/qa")
    public ResponseEntity<ApiResponse<KnowledgeQAResponse>> knowledgeQA(
            @Valid @RequestBody KnowledgeQARequest request) {
        KnowledgeQAResponse response = claudeService.knowledgeQA(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logs/analyze")
    public ResponseEntity<ApiResponse<LogAnalysisResponse>> analyzeLogs(
            @Valid @RequestBody LogAnalysisRequest request) {
        LogAnalysisResponse response = claudeService.analyzeLogs(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/summarize")
    public ResponseEntity<ApiResponse<String>> summarize(
            @RequestParam String text,
            @RequestParam(defaultValue = "200") int maxLength) {
        String summary = claudeService.summarize(text, maxLength);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/keywords")
    public ResponseEntity<ApiResponse<String>> extractKeywords(
            @RequestParam String text,
            @RequestParam(defaultValue = "10") int count) {
        String keywords = claudeService.extractKeywords(text, count);
        return ResponseEntity.ok(ApiResponse.success(keywords));
    }
}
