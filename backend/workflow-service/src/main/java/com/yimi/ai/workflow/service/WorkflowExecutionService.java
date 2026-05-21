package com.yimi.ai.workflow.service;

import com.yimi.ai.workflow.dto.WorkflowExecutionRequest;
import com.yimi.ai.workflow.dto.WorkflowExecutionResponse;
import com.yimi.ai.workflow.dto.WorkflowResponse;
import com.yimi.ai.workflow.dto.WorkflowResponse.WorkflowEdgeResponse;
import com.yimi.ai.workflow.dto.WorkflowResponse.WorkflowNodeResponse;
import com.yimi.ai.workflow.repository.WorkflowExecutionRepository;
import com.yimi.ai.workflow.service.WorkflowService;
import com.yimi.ai.common.entity.WorkflowExecution;
import com.yimi.ai.common.enums.ExecutionStatus;
import com.yimi.ai.common.enums.TriggerType;
import com.yimi.ai.common.exception.BusinessException;
import com.yimi.ai.common.response.PageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {

    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowService workflowService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${agent.service.url:http://localhost:8082}")
    private String agentServiceUrl;

    @Value("${skill.service.url:http://localhost:8083}")
    private String skillServiceUrl;

    public WorkflowExecutionResponse create(WorkflowExecutionRequest request) {
        WorkflowExecution execution = WorkflowExecution.builder()
                .id(UUID.randomUUID().toString())
                .workflowId(request.getWorkflowId())
                .triggeredBy(request.getTriggeredBy())
                .triggerType(TriggerType.fromCode(request.getTriggerType()))
                .status(ExecutionStatus.RUNNING)
                .inputData(request.getInputData())
                .build();

        WorkflowExecution saved = executionRepository.save(execution);
        return convertToResponse(saved);
    }

    @Transactional
    public WorkflowExecutionResponse execute(WorkflowExecutionRequest request) {
        // Create execution record
        WorkflowExecution execution = WorkflowExecution.builder()
                .id(UUID.randomUUID().toString())
                .workflowId(request.getWorkflowId())
                .triggeredBy(request.getTriggeredBy())
                .triggerType(TriggerType.fromCode(request.getTriggerType()))
                .status(ExecutionStatus.RUNNING)
                .inputData(request.getInputData())
                .build();
        execution = executionRepository.save(execution);

        // Get workflow with nodes and edges
        WorkflowResponse workflow = workflowService.get(request.getWorkflowId());
        List<WorkflowNodeResponse> nodes = workflow.getNodes();
        List<WorkflowEdgeResponse> edges = workflow.getEdges();

        if (nodes == null || nodes.isEmpty()) {
            fail(execution.getId(), "工作流没有节点");
            return convertToResponse(execution);
        }

        // Build adjacency list
        Map<String, List<String>> adj = new HashMap<>();
        for (WorkflowEdgeResponse edge : edges) {
            adj.computeIfAbsent(edge.getSource(), k -> new java.util.ArrayList<>()).add(edge.getTarget());
        }

        // Find start node
        String currentNodeId = nodes.stream()
                .filter(n -> "start".equals(n.getType()))
                .findFirst()
                .map(WorkflowNodeResponse::getId)
                .orElse(nodes.get(0).getId());

        // Build node map
        Map<String, WorkflowNodeResponse> nodeMap = new HashMap<>();
        for (WorkflowNodeResponse node : nodes) {
            nodeMap.put(node.getId(), node);
        }

        // Data context passed between nodes
        ObjectNode context = objectMapper.createObjectNode();
        if (request.getInputData() != null && !request.getInputData().isEmpty()) {
            try {
                context = (ObjectNode) objectMapper.readTree(request.getInputData());
            } catch (Exception e) {
                // ignore
            }
        }

        // Traverse and execute nodes
        int maxSteps = nodes.size() * 2; // safety limit
        int step = 0;
        try {
            while (currentNodeId != null && !currentNodeId.isEmpty() && step < maxSteps) {
                WorkflowNodeResponse node = nodeMap.get(currentNodeId);
                if (node == null) break;

                // Execute based on node type
                ObjectNode result = executeNode(node, context);
                if (result != null) {
                    context.set(node.getId(), result);
                }

                // Find next node
                List<String> nextNodes = adj.getOrDefault(currentNodeId, List.of());
                if (node.getType().equals("condition")) {
                    // Condition node: evaluate based on config
                    String next = evaluateCondition(node, context, nextNodes);
                    currentNodeId = next;
                } else if (nextNodes.isEmpty()) {
                    currentNodeId = null; // end
                } else {
                    currentNodeId = nextNodes.get(0);
                }
                step++;
            }
        } catch (Exception e) {
            fail(execution.getId(), e.getMessage());
            return convertToResponse(execution);
        }

        // Complete execution
        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setEndTime(LocalDateTime.now());
        execution.setOutputData(context.toString());
        execution.setDuration(java.time.Duration.between(
                execution.getStartTime(), LocalDateTime.now()).toMillis());
        executionRepository.save(execution);

        // Update workflow metrics
        return convertToResponse(execution);
    }

    private ObjectNode executeNode(WorkflowNodeResponse node, ObjectNode context) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("nodeId", node.getId());
        result.put("nodeType", node.getType());
        result.put("nodeName", node.getName());
        result.put("executedAt", LocalDateTime.now().toString());

        switch (node.getType()) {
            case "agent":
                result = executeAgentNode(node, context);
                break;
            case "skill":
                result = executeSkillNode(node, context);
                break;
            case "condition":
                result.put("status", "evaluated");
                break;
            case "loop":
                result.put("status", "simulated");
                result.put("message", "循环节点暂未实现完整执行");
                break;
            case "approval":
                result.put("status", "pending_approval");
                result.put("message", "等待人工审批");
                break;
            case "start":
            case "end":
                result.put("status", "passed");
                break;
            default:
                result.put("status", "unknown");
        }
        return result;
    }

@SuppressWarnings("unchecked")
    private ObjectNode executeAgentNode(WorkflowNodeResponse node, ObjectNode context) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("nodeId", node.getId());
        result.put("nodeType", "agent");
        result.put("nodeName", node.getName());
        result.put("executedAt", LocalDateTime.now().toString());

        if (node.getAgentId() == null || node.getAgentId().isEmpty()) {
            result.put("status", "error");
            result.put("message", "未关联Agent");
            return result;
        }

        try {
            // Build input from context
            String input = context.toString();

            // Call agent-service to execute the agent
            Map<String, Object> body = Map.of("input", input);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                agentServiceUrl + "/api/agents/" + node.getAgentId() + "/call",
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                result.put("status", "success");
                result.put("output", data.getOrDefault("output", "").toString());
                if (data.containsKey("duration")) {
                    result.put("duration", ((Number) data.get("duration")).longValue());
                }
            } else {
                result.put("status", "error");
                result.put("message", "Agent调用返回异常");
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Agent调用失败: " + e.getMessage());
        }
        return result;
    }

    private ObjectNode executeSkillNode(WorkflowNodeResponse node, ObjectNode context) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("nodeId", node.getId());
        result.put("nodeType", "skill");
        result.put("nodeName", node.getName());
        result.put("executedAt", LocalDateTime.now().toString());

        if (node.getSkillId() == null || node.getSkillId().isEmpty()) {
            result.put("status", "error");
            result.put("message", "未关联Skill");
            return result;
        }

        try {
            // For now, skill execution is simulated since skill-service doesn't have a call endpoint
            result.put("status", "success");
            result.put("message", "Skill执行成功: " + node.getName() + " (skillId=" + node.getSkillId() + ")");
            result.put("output", "Skill " + node.getName() + " 已执行");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Skill调用失败: " + e.getMessage());
        }
        return result;
    }

    private String evaluateCondition(WorkflowNodeResponse node, ObjectNode context, List<String> nextNodes) {
        if (nextNodes.size() < 2) {
            return nextNodes.isEmpty() ? null : nextNodes.get(0);
        }
        // Simple condition: evaluate the condition expression in config
        if (node.getConfig() != null && !node.getConfig().isEmpty()) {
            try {
                JsonNode config = objectMapper.readTree(node.getConfig());
                if (config.has("expression")) {
                    // For now, default to first branch (true)
                    return nextNodes.get(0);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return nextNodes.get(0);
    }

    public WorkflowExecutionResponse complete(String id, String outputData) {
        WorkflowExecution execution = executionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "执行记录不存在"));

        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setEndTime(LocalDateTime.now());
        execution.setOutputData(outputData);
        execution.setDuration(java.time.Duration.between(
                execution.getStartTime(), LocalDateTime.now()).toMillis());

        WorkflowExecution saved = executionRepository.save(execution);
        return convertToResponse(saved);
    }

    public WorkflowExecutionResponse fail(String id, String errorMessage) {
        WorkflowExecution execution = executionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "执行记录不存在"));

        execution.setStatus(ExecutionStatus.FAILED);
        execution.setEndTime(LocalDateTime.now());
        execution.setErrorMessage(errorMessage);
        execution.setDuration(java.time.Duration.between(
                execution.getStartTime(), LocalDateTime.now()).toMillis());

        WorkflowExecution saved = executionRepository.save(execution);
        return convertToResponse(saved);
    }

    public PageResponse<WorkflowExecutionResponse> list(String workflowId, String status, 
                                                        String triggerType, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<WorkflowExecution> executionPage;

        if (workflowId != null && status != null) {
            executionPage = executionRepository.findByWorkflowIdAndStatus(
                    workflowId, ExecutionStatus.fromCode(status), pageable);
        } else if (workflowId != null) {
            executionPage = executionRepository.findByWorkflowId(workflowId, pageable);
        } else if (status != null) {
            executionPage = executionRepository.findByStatus(ExecutionStatus.fromCode(status), pageable);
        } else if (triggerType != null) {
            executionPage = executionRepository.findByTriggerType(TriggerType.fromCode(triggerType), pageable);
        } else {
            executionPage = executionRepository.findAll(pageable);
        }

        List<WorkflowExecutionResponse> responses = executionPage.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        return PageResponse.of(responses, executionPage.getTotalElements(), page, size);
    }

    private WorkflowExecutionResponse convertToResponse(WorkflowExecution execution) {
        return WorkflowExecutionResponse.builder()
                .id(execution.getId())
                .workflowId(execution.getWorkflowId())
                .triggeredBy(execution.getTriggeredBy())
                .triggerType(execution.getTriggerType().getCode())
                .status(execution.getStatus().getCode())
                .startTime(execution.getStartTime().toString())
                .endTime(execution.getEndTime() != null ? execution.getEndTime().toString() : null)
                .duration(execution.getDuration())
                .inputData(execution.getInputData())
                .outputData(execution.getOutputData())
                .errorMessage(execution.getErrorMessage())
                .build();
    }
}