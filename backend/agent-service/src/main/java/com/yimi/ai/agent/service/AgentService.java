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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.service.url:http://localhost:8087}")
    private String llmServiceUrl;

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
                .filter(r -> r != null)
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
                .creatorId("system")
                .status(request.getStatus() != null ? AgentStatus.fromCode(request.getStatus()) : AgentStatus.PENDING)
                .isFavorite(request.getIsFavorite() != null ? request.getIsFavorite() : false)
                .successRate(request.getSuccessRate() != null ? BigDecimal.valueOf(request.getSuccessRate()) : BigDecimal.ZERO)
                .avgTime(request.getAvgTime() != null ? BigDecimal.valueOf(request.getAvgTime()) : BigDecimal.ZERO)
                .dailyCalls(request.getDailyCalls() != null ? request.getDailyCalls() : 0)
                .usageCount(request.getUsageCount() != null ? request.getUsageCount() : 0)
                .rating(request.getRating() != null ? BigDecimal.valueOf(request.getRating()) : null)
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

        String output = callLlm(agent.getName(), agent.getDescription(), request.getInput());

        long duration = System.currentTimeMillis() - startTime;

        agent.setUsageCount(agent.getUsageCount() + 1);
        agent.setDailyCalls(agent.getDailyCalls() + 1);

        int usageCount = agent.getUsageCount();
        agent.setSuccessRate(agent.getSuccessRate().multiply(BigDecimal.valueOf(usageCount - 1))
                .add(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(usageCount), 2, RoundingMode.HALF_UP));
        agent.setAvgTime(agent.getAvgTime().multiply(BigDecimal.valueOf(usageCount - 1))
                .add(BigDecimal.valueOf(duration))
                .divide(BigDecimal.valueOf(usageCount), 2, RoundingMode.HALF_UP));
        agentRepository.save(agent);

        return AgentCallResponse.builder()
                .output(output)
                .duration(duration)
                .build();
    }

    @SuppressWarnings("unchecked")
    private String callLlm(String agentName, String agentDescription, String userInput) {
        try {
            Map<String, Object> body = Map.of(
                "messages", List.of(
                    Map.of("role", "system", "content",
                        String.format("你是一个专业的AI助手，扮演角色：%s。角色描述：%s。请用专业、友好的语气回答问题。", agentName, agentDescription)),
                    Map.of("role", "user", "content", userInput)
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                llmServiceUrl + "/api/llm/chat",
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                if (data != null && data.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) data.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        return (String) message.get("content");
                    }
                }
            }
            return "抱歉，AI服务暂时无法响应。";
        } catch (Exception e) {
            return "AI服务调用失败：" + e.getMessage();
        }
    }

    private AgentResponse convertToResponse(Agent agent) {
        if (agent == null) {
            return null;
        }
        List<String> agentTags = Collections.emptyList();
        try {
            agentTags = deserializeTags(agent.getTags());
        } catch (Exception e) {
            agentTags = Collections.emptyList();
        }
        
        String departmentCode = null;
        if (agent.getDepartment() != null) {
            departmentCode = agent.getDepartment().getCode();
        }
        
        String statusCode = null;
        if (agent.getStatus() != null) {
            statusCode = agent.getStatus().getCode();
        }
        
        return AgentResponse.builder()
                .id(agent.getId())
                .name(agent.getName())
                .description(agent.getDescription())
                .department(departmentCode)
                .tags(agentTags)
                .successRate(agent.getSuccessRate() != null ? agent.getSuccessRate().doubleValue() : null)
                .avgTime(agent.getAvgTime() != null ? agent.getAvgTime().doubleValue() : null)
                .dailyCalls(agent.getDailyCalls())
                .usageCount(agent.getUsageCount())
                .creatorId(agent.getCreatorId())
                .createdAt(agent.getCreatedAt() != null ? agent.getCreatedAt().toString() : null)
                .status(statusCode)
                .isFavorite(agent.getIsFavorite())
                .rating(agent.getRating() != null ? agent.getRating().doubleValue() : null)
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