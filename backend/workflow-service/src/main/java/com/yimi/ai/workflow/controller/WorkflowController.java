package com.yimi.ai.workflow.controller;

import com.yimi.ai.workflow.dto.WorkflowCreateRequest;
import com.yimi.ai.workflow.dto.WorkflowExecutionRequest;
import com.yimi.ai.workflow.dto.WorkflowExecutionResponse;
import com.yimi.ai.workflow.dto.WorkflowResponse;
import com.yimi.ai.workflow.service.WorkflowExecutionService;
import com.yimi.ai.workflow.service.WorkflowService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowExecutionService workflowExecutionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WorkflowResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String triggerType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResponse<WorkflowResponse> response = workflowService.list(status, triggerType, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> get(@PathVariable String id) {
        WorkflowResponse response = workflowService.get(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkflowResponse>> create(@Valid @RequestBody WorkflowCreateRequest request) {
        WorkflowResponse response = workflowService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> update(
            @PathVariable String id, 
            @Valid @RequestBody WorkflowCreateRequest request) {
        WorkflowResponse response = workflowService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        workflowService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<WorkflowResponse>> activate(@PathVariable String id) {
        WorkflowResponse response = workflowService.activate(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<WorkflowResponse>> deactivate(@PathVariable String id) {
        WorkflowResponse response = workflowService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiResponse<WorkflowExecutionResponse>> execute(
            @PathVariable String id,
            @RequestBody(required = false) WorkflowExecutionRequest request) {
        if (request == null) {
            request = new WorkflowExecutionRequest();
            request.setWorkflowId(id);
            request.setTriggerType("api");
        }
        request.setWorkflowId(id);
        WorkflowExecutionResponse response = workflowExecutionService.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}