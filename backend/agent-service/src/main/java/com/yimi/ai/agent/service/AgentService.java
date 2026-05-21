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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.service.url:http://localhost:8087}")
    private String llmServiceUrl;

    @Value("${skill.service.url:http://localhost:8092}")
    private String skillServiceUrl;

    @Value("${knowledge.service.url:http://localhost:8093}")
    private String knowledgeServiceUrl;

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

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public AgentCallResponse call(String id, AgentCallRequest request) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Agent不存在"));

        if (agent.getStatus() == AgentStatus.OFFLINE) {
            throw new BusinessException(503, "Agent当前不可用");
        }

        long startTime = System.currentTimeMillis();

        List<String> agentSkills = deserializeJsonArray(agent.getSkills());
        List<String> agentKnowledge = deserializeJsonArray(agent.getKnowledge());

        CompletableFuture<String> skillFuture = CompletableFuture.supplyAsync(() -> {
            if (!agentSkills.isEmpty()) {
                return callSkills(agentSkills, request.getInput());
            }
            return "";
        }, executorService);

        CompletableFuture<String> knowledgeFuture = CompletableFuture.supplyAsync(() -> {
            if (!agentKnowledge.isEmpty()) {
                return searchKnowledge(agentKnowledge, request.getInput());
            }
            return "";
        }, executorService);

        CompletableFuture.allOf(skillFuture, knowledgeFuture).join();

        String skillResults = "";
        String knowledgeContent = "";
        try {
            skillResults = skillFuture.get();
            knowledgeContent = knowledgeFuture.get();
        } catch (Exception e) {
            System.err.println("Failed to get async results: " + e.getMessage());
        }

        String output = callLlmWithTools(agent, request.getInput(), skillResults, knowledgeContent);

        long duration = System.currentTimeMillis() - startTime;

        try {
            agent.setUsageCount(agent.getUsageCount() + 1);
            agent.setDailyCalls(agent.getDailyCalls() + 1);

            int usageCount = agent.getUsageCount();
            double newSuccessRate = 100.0;
            double newAvgTime = duration;

            if (usageCount > 1) {
                double currentSuccessRate = agent.getSuccessRate() != null ? agent.getSuccessRate().doubleValue() : 0;
                double currentAvgTime = agent.getAvgTime() != null ? agent.getAvgTime().doubleValue() : 0;

                newSuccessRate = (currentSuccessRate * (usageCount - 1) + 100) / usageCount;
                newAvgTime = (currentAvgTime * (usageCount - 1) + duration) / usageCount;
            }

            agent.setSuccessRate(BigDecimal.valueOf(newSuccessRate));
            agent.setAvgTime(BigDecimal.valueOf(newAvgTime));
            agentRepository.save(agent);
        } catch (Exception e) {
            System.err.println("Failed to update agent stats: " + e.getMessage());
        }

        return AgentCallResponse.builder()
                .output(output)
                .duration(duration)
                .build();
    }

    private List<String> deserializeJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String callSkills(List<String> skillIds, String userInput) {
        StringBuilder results = new StringBuilder();
        results.append("技能调用结果：\n");

        for (String skillId : skillIds) {
            try {
                Map<String, Object> body = Map.of("input", userInput);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                    skillServiceUrl + "/api/skills/" + skillId + "/execute",
                    HttpMethod.POST,
                    entity,
                    Map.class
                );

                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("data")) {
                    results.append("- 技能[").append(skillId).append("]: ").append(responseBody.get("data")).append("\n");
                }
            } catch (Exception e) {
                results.append("- 技能[").append(skillId).append("]调用失败: ").append(e.getMessage()).append("\n");
            }
        }

        return results.toString();
    }

    private String searchKnowledge(List<String> knowledgeIds, String query) {
        StringBuilder results = new StringBuilder();
        results.append("知识库检索结果：\n");

        for (String kbId : knowledgeIds) {
            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(
                    knowledgeServiceUrl + "/api/knowledge/search?query=" + query + "&limit=5",
                    Map.class
                );

                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("data")) {
                    Object data = responseBody.get("data");
                    if (data instanceof List) {
                        List<?> docs = (List<?>) data;
                        for (Object doc : docs) {
                            if (doc instanceof Map) {
                                Map<?, ?> docMap = (Map<?, ?>) doc;
                                String title = docMap.get("title") != null ? docMap.get("title").toString() : "未知标题";
                                String content = docMap.get("content") != null ? docMap.get("content").toString() : "";
                                if (content.length() > 500) {
                                    content = content.substring(0, 500) + "...";
                                }
                                results.append("- ").append(title).append(":\n").append(content).append("\n\n");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                results.append("- 知识库[").append(kbId).append("]检索失败: ").append(e.getMessage()).append("\n");
            }
        }

        return results.toString();
    }

    @SuppressWarnings("unchecked")
    private String callLlmWithTools(Agent agent, String userInput, String skillResults, String knowledgeContent) {
        try {
            StringBuilder systemPrompt = new StringBuilder();
            systemPrompt.append("你是一个专业的AI助手，扮演角色：").append(agent.getName()).append("。\n");
            if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isEmpty()) {
                systemPrompt.append("系统指令：").append(agent.getSystemPrompt()).append("\n");
            } else {
                systemPrompt.append("角色描述：").append(agent.getDescription() != null ? agent.getDescription() : "").append("\n");
            }
            systemPrompt.append("请用专业、友好的语气回答问题。\n");

            if (!skillResults.isEmpty()) {
                systemPrompt.append("\n以下是相关技能调用结果，请参考这些数据进行回答：\n");
                systemPrompt.append(skillResults).append("\n");
            }

            if (!knowledgeContent.isEmpty()) {
                systemPrompt.append("\n以下是知识库检索结果，请基于这些内容进行回答：\n");
                systemPrompt.append(knowledgeContent).append("\n");
            }

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt.toString()));

            if (agent.getExamples() != null && !agent.getExamples().isEmpty()) {
                try {
                    List<Map<String, String>> examples = objectMapper.readValue(
                        agent.getExamples(),
                        new TypeReference<List<Map<String, String>>>() {}
                    );
                    for (Map<String, String> example : examples) {
                        messages.add(Map.of("role", "user", "content", example.get("input")));
                        messages.add(Map.of("role", "assistant", "content", example.get("output")));
                    }
                } catch (Exception e) {
                    // ignore examples parsing error
                }
            }

            messages.add(Map.of("role", "user", "content", userInput));

            Map<String, Object> body = Map.of("messages", messages);

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
        
        List<String> agentSkills = deserializeJsonArray(agent.getSkills());
        List<String> agentKnowledge = deserializeJsonArray(agent.getKnowledge());
        
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
                .skills(agentSkills)
                .knowledge(agentKnowledge)
                .systemPrompt(agent.getSystemPrompt())
                .model(agent.getModel())
                .visibility(agent.getVisibility())
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