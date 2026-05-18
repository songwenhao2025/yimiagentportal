package com.yimi.ai.agent.service;

import com.yimi.ai.agent.dto.AgentCallRequest;
import com.yimi.ai.agent.dto.AgentCallResponse;
import com.yimi.ai.agent.dto.AgentCreateRequest;
import com.yimi.ai.agent.dto.AgentResponse;
import com.yimi.ai.agent.repository.AgentRepository;
import com.yimi.ai.common.entity.Agent;
import com.yimi.ai.common.enums.AgentStatus;
import com.yimi.ai.common.enums.Department;
import com.yimi.ai.common.exception.BusinessException;
import com.yimi.ai.common.response.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PageResponse<AgentResponse> list(String department, String status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Agent> agentPage;

        if (keyword != null && !keyword.isEmpty()) {
            agentPage = agentRepository.findByNameContaining(keyword, pageable);
        } else if (department != null && status != null) {
            agentPage = agentRepository.findByDepartmentAndStatus(
                    Department.fromCode(department), 
                    AgentStatus.fromCode(status), 
                    pageable
            );
        } else if (department != null) {
            agentPage = agentRepository.findByDepartment(Department.fromCode(department), pageable);
        } else if (status != null) {
            agentPage = agentRepository.findByStatus(AgentStatus.fromCode(status), pageable);
        } else {
            agentPage = agentRepository.findAll(pageable);
        }

        List<AgentResponse> responses = agentPage.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        return PageResponse.of(responses, agentPage.getTotalElements(), page, size);
    }

    public AgentResponse get(String id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Agent不存在"));
        return convertToResponse(agent);
    }

    public AgentResponse create(AgentCreateRequest request) {
        Agent agent = Agent.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .department(Department.fromCode(request.getDepartment()))
                .tags(serializeTags(request.getTags()))
                .creatorId(request.getCreatorId())
                .status(AgentStatus.PENDING)
                .isFavorite(false)
                .successRate(0.0)
                .avgTime(0.0)
                .dailyCalls(0)
                .usageCount(0)
                .build();

        Agent saved = agentRepository.save(agent);
        return convertToResponse(saved);
    }

    public AgentResponse update(String id, AgentCreateRequest request) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Agent不存在"));

        agent.setName(request.getName());
        agent.setDescription(request.getDescription());
        agent.setDepartment(Department.fromCode(request.getDepartment()));
        agent.setTags(serializeTags(request.getTags()));

        Agent saved = agentRepository.save(agent);
        return convertToResponse(saved);
    }

    public void delete(String id) {
        if (!agentRepository.existsById(id)) {
            throw new BusinessException(404, "Agent不存在");
        }
        agentRepository.deleteById(id);
    }

    public AgentCallResponse call(String id, AgentCallRequest request) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Agent不存在"));

        if (agent.getStatus() != AgentStatus.ONLINE) {
            throw new BusinessException(503, "Agent当前不可用");
        }

        long startTime = System.currentTimeMillis();
        
        String output = simulateAgentCall(agent.getName(), request.getInput());
        
        long duration = System.currentTimeMillis() - startTime;

        agent.setUsageCount(agent.getUsageCount() + 1);
        agent.setDailyCalls(agent.getDailyCalls() + 1);
        agent.setSuccessRate((agent.getSuccessRate() * (agent.getUsageCount() - 1) + 100) / agent.getUsageCount());
        agent.setAvgTime((agent.getAvgTime() * (agent.getUsageCount() - 1) + duration) / agent.getUsageCount());
        agentRepository.save(agent);

        return AgentCallResponse.builder()
                .output(output)
                .duration(duration)
                .build();
    }

    private String simulateAgentCall(String agentName, String input) {
        return String.format("您好！我是%s，已收到您的请求：%s\n\n这是我的响应内容...", agentName, input);
    }

    private AgentResponse convertToResponse(Agent agent) {
        return AgentResponse.builder()
                .id(agent.getId())
                .name(agent.getName())
                .description(agent.getDescription())
                .department(agent.getDepartment().getCode())
                .tags(deserializeTags(agent.getTags()))
                .successRate(agent.getSuccessRate())
                .avgTime(agent.getAvgTime())
                .dailyCalls(agent.getDailyCalls())
                .usageCount(agent.getUsageCount())
                .creatorId(agent.getCreatorId())
                .createdAt(agent.getCreatedAt().toString())
                .status(agent.getStatus().getCode())
                .isFavorite(agent.getIsFavorite())
                .rating(agent.getRating())
                .build();
    }

    private String serializeTags(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> deserializeTags(String tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(tags, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}