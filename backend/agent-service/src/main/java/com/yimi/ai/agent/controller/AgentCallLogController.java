package com.yimi.ai.agent.controller;

import com.yimi.ai.agent.dto.AgentCallLogResponse;
import com.yimi.ai.agent.service.AgentCallLogService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent-call-logs")
@RequiredArgsConstructor
public class AgentCallLogController {

    private final AgentCallLogService logService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AgentCallLogResponse>>> list(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<AgentCallLogResponse> response = logService.list(agentId, userId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}