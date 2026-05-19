package com.yimi.ai.agent.controller;

import com.yimi.ai.agent.dto.AgentRatingRequest;
import com.yimi.ai.agent.dto.AgentRatingResponse;
import com.yimi.ai.agent.service.AgentRatingService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents/{agentId}/ratings")
@RequiredArgsConstructor
public class AgentRatingController {

    private final AgentRatingService ratingService;

    @PostMapping
    public ResponseEntity<ApiResponse<AgentRatingResponse>> create(
            @PathVariable String agentId,
            @RequestParam String userId,
            @RequestBody AgentRatingRequest request) {
        AgentRatingResponse response = ratingService.create(agentId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<AgentRatingResponse>> update(
            @PathVariable String agentId,
            @RequestParam String userId,
            @RequestBody AgentRatingRequest request) {
        AgentRatingResponse response = ratingService.update(agentId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        ratingService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AgentRatingResponse>>> list(
            @PathVariable String agentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<AgentRatingResponse> response = ratingService.list(agentId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}