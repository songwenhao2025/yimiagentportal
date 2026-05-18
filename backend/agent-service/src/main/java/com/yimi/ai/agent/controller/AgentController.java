package com.yimi.ai.agent.controller;

import com.yimi.ai.agent.dto.AgentCallRequest;
import com.yimi.ai.agent.dto.AgentCallResponse;
import com.yimi.ai.agent.dto.AgentCreateRequest;
import com.yimi.ai.agent.dto.AgentResponse;
import com.yimi.ai.agent.service.AgentService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AgentResponse>>> list(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResponse<AgentResponse> response = agentService.list(department, status, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentResponse>> get(@PathVariable String id) {
        AgentResponse response = agentService.get(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AgentResponse>> create(@Valid @RequestBody AgentCreateRequest request) {
        AgentResponse response = agentService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentResponse>> update(
            @PathVariable String id, 
            @Valid @RequestBody AgentCreateRequest request) {
        AgentResponse response = agentService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        agentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/call")
    public ResponseEntity<ApiResponse<AgentCallResponse>> call(
            @PathVariable String id, 
            @RequestBody AgentCallRequest request) {
        AgentCallResponse response = agentService.call(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}