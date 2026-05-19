package com.yimi.ai.agent.service;

import com.yimi.ai.agent.dto.AgentCallLogResponse;
import com.yimi.ai.agent.repository.AgentCallLogRepository;
import com.yimi.ai.common.entity.AgentCallLog;
import com.yimi.ai.common.enums.CallStatus;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentCallLogService {

    private final AgentCallLogRepository logRepository;

    public AgentCallLogResponse create(String agentId, String userId, String input, 
                                       String output, CallStatus status, Integer duration,
                                       Integer tokenUsage, String errorMessage) {
        AgentCallLog log = AgentCallLog.builder()
                .id(UUID.randomUUID().toString())
                .agentId(agentId)
                .userId(userId)
                .input(input)
                .output(output)
                .status(status)
                .duration(duration)
                .tokenUsage(tokenUsage)
                .errorMessage(errorMessage)
                .build();

        AgentCallLog saved = logRepository.save(log);
        return convertToResponse(saved);
    }

    public PageResponse<AgentCallLogResponse> list(String agentId, String userId, 
                                                   String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AgentCallLog> logPage;

        if (agentId != null && status != null) {
            logPage = logRepository.findByAgentIdAndStatus(agentId, CallStatus.fromCode(status), pageable);
        } else if (agentId != null) {
            logPage = logRepository.findByAgentId(agentId, pageable);
        } else if (userId != null) {
            logPage = logRepository.findByUserId(userId, pageable);
        } else {
            logPage = logRepository.findAll(pageable);
        }

        List<AgentCallLogResponse> responses = logPage.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        return PageResponse.of(responses, logPage.getTotalElements(), page, size);
    }

    private AgentCallLogResponse convertToResponse(AgentCallLog log) {
        return AgentCallLogResponse.builder()
                .id(log.getId())
                .agentId(log.getAgentId())
                .userId(log.getUserId())
                .input(log.getInput())
                .output(log.getOutput())
                .status(log.getStatus().getCode())
                .duration(log.getDuration())
                .tokenUsage(log.getTokenUsage())
                .errorMessage(log.getErrorMessage())
                .createdAt(log.getCreatedAt().toString())
                .build();
    }
}