package com.yimi.ai.workflow.controller;

import com.yimi.ai.workflow.dto.WorkflowExecutionRequest;
import com.yimi.ai.workflow.dto.WorkflowExecutionResponse;
import com.yimi.ai.workflow.service.WorkflowExecutionService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow-executions")
@RequiredArgsConstructor
public class WorkflowExecutionController {

    private final WorkflowExecutionService executionService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkflowExecutionResponse>> create(
            @RequestBody WorkflowExecutionRequest request) {
        WorkflowExecutionResponse response = executionService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<WorkflowExecutionResponse>> complete(
            @PathVariable String id,
            @RequestBody String outputData) {
        WorkflowExecutionResponse response = executionService.complete(id, outputData);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/fail")
    public ResponseEntity<ApiResponse<WorkflowExecutionResponse>> fail(
            @PathVariable String id,
            @RequestBody String errorMessage) {
        WorkflowExecutionResponse response = executionService.fail(id, errorMessage);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WorkflowExecutionResponse>>> list(
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String triggerType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<WorkflowExecutionResponse> response = executionService.list(
                workflowId, status, triggerType, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}