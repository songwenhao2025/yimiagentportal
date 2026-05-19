package com.yimi.ai.workflow.service;

import com.yimi.ai.workflow.dto.WorkflowCreateRequest;
import com.yimi.ai.workflow.dto.WorkflowResponse;
import com.yimi.ai.workflow.repository.WorkflowExecutionRepository;
import com.yimi.ai.workflow.repository.WorkflowRepository;
import com.yimi.ai.common.entity.Workflow;
import com.yimi.ai.common.entity.WorkflowEdge;
import com.yimi.ai.common.entity.WorkflowNode;
import com.yimi.ai.common.enums.ExecutionStatus;
import com.yimi.ai.common.enums.NodeType;
import com.yimi.ai.common.enums.TriggerType;
import com.yimi.ai.common.enums.WorkflowStatus;
import com.yimi.ai.common.exception.BusinessException;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;

    public PageResponse<WorkflowResponse> list(String status, String triggerType, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Workflow> workflowPage;

        if (keyword != null && !keyword.isEmpty()) {
            workflowPage = workflowRepository.findByNameContaining(keyword, pageable);
        } else if (status != null) {
            workflowPage = workflowRepository.findByStatus(WorkflowStatus.fromCode(status), pageable);
        } else if (triggerType != null) {
            workflowPage = workflowRepository.findByTriggerType(TriggerType.fromCode(triggerType), pageable);
        } else {
            workflowPage = workflowRepository.findAll(pageable);
        }

        List<WorkflowResponse> responses = workflowPage.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        return PageResponse.of(responses, workflowPage.getTotalElements(), page, size);
    }

    public WorkflowResponse get(String id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "工作流不存在"));
        return convertToResponse(workflow);
    }

    @Transactional
    public WorkflowResponse create(WorkflowCreateRequest request) {
        Workflow workflow = Workflow.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .creatorId(request.getCreatorId() != null ? request.getCreatorId() : "system")
                .status(WorkflowStatus.DRAFT)
                .triggerType(TriggerType.fromCode(request.getTriggerType()))
                .cronExpression(request.getCronExpression())
                .executionCount(0)
                .successRate(BigDecimal.ZERO)
                .build();

        Workflow saved = workflowRepository.save(workflow);

        // Save nodes
        if (request.getNodes() != null) {
            for (WorkflowCreateRequest.WorkflowNodeRequest nodeReq : request.getNodes()) {
                WorkflowNode node = WorkflowNode.builder()
                        .id(nodeReq.getId() != null ? nodeReq.getId() : UUID.randomUUID().toString())
                        .workflowId(saved.getId())
                        .type(NodeType.fromCode(nodeReq.getType()))
                        .name(nodeReq.getName() != null ? nodeReq.getName() : nodeReq.getType())
                        .config(nodeReq.getConfig())
                        .positionX(nodeReq.getX() != null ? nodeReq.getX() : 0)
                        .positionY(nodeReq.getY() != null ? nodeReq.getY() : 0)
                        .agentId(nodeReq.getAgentId())
                        .skillId(nodeReq.getSkillId())
                        .build();
                saved.getNodes().add(node);
            }
        }

        // Save edges
        if (request.getEdges() != null) {
            for (WorkflowCreateRequest.WorkflowEdgeRequest edgeReq : request.getEdges()) {
                WorkflowEdge edge = WorkflowEdge.builder()
                        .id(edgeReq.getId() != null ? edgeReq.getId() : UUID.randomUUID().toString())
                        .workflowId(saved.getId())
                        .sourceNodeId(edgeReq.getSource() != null ? edgeReq.getSource() : "")
                        .targetNodeId(edgeReq.getTarget() != null ? edgeReq.getTarget() : "")
                        .sourceHandle(edgeReq.getSourceHandle())
                        .targetHandle(edgeReq.getTargetHandle())
                        .build();
                saved.getEdges().add(edge);
            }
        }

        saved = workflowRepository.save(saved);
        return convertToResponse(saved);
    }

    @Transactional
    public WorkflowResponse update(String id, WorkflowCreateRequest request) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "工作流不存在"));

        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setTriggerType(TriggerType.fromCode(request.getTriggerType()));
        workflow.setCronExpression(request.getCronExpression());

        // Replace nodes
        workflow.getNodes().clear();
        if (request.getNodes() != null) {
            for (WorkflowCreateRequest.WorkflowNodeRequest nodeReq : request.getNodes()) {
                WorkflowNode node = WorkflowNode.builder()
                        .id(nodeReq.getId() != null ? nodeReq.getId() : UUID.randomUUID().toString())
                        .workflowId(workflow.getId())
                        .type(NodeType.fromCode(nodeReq.getType()))
                        .name(nodeReq.getName() != null ? nodeReq.getName() : nodeReq.getType())
                        .config(nodeReq.getConfig())
                        .positionX(nodeReq.getX() != null ? nodeReq.getX() : 0)
                        .positionY(nodeReq.getY() != null ? nodeReq.getY() : 0)
                        .agentId(nodeReq.getAgentId())
                        .skillId(nodeReq.getSkillId())
                        .build();
                workflow.getNodes().add(node);
            }
        }

        // Replace edges
        workflow.getEdges().clear();
        if (request.getEdges() != null) {
            for (WorkflowCreateRequest.WorkflowEdgeRequest edgeReq : request.getEdges()) {
                WorkflowEdge edge = WorkflowEdge.builder()
                        .id(edgeReq.getId() != null ? edgeReq.getId() : UUID.randomUUID().toString())
                        .workflowId(workflow.getId())
                        .sourceNodeId(edgeReq.getSource() != null ? edgeReq.getSource() : "")
                        .targetNodeId(edgeReq.getTarget() != null ? edgeReq.getTarget() : "")
                        .sourceHandle(edgeReq.getSourceHandle())
                        .targetHandle(edgeReq.getTargetHandle())
                        .build();
                workflow.getEdges().add(edge);
            }
        }

        Workflow saved = workflowRepository.save(workflow);
        return convertToResponse(saved);
    }

    public void delete(String id) {
        if (!workflowRepository.existsById(id)) {
            throw new BusinessException(404, "工作流不存在");
        }
        workflowRepository.deleteById(id);
    }

    public WorkflowResponse activate(String id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "工作流不存在"));
        
        if (workflow.getStatus() == WorkflowStatus.ACTIVE) {
            throw new BusinessException(400, "工作流已激活");
        }

        workflow.setStatus(WorkflowStatus.ACTIVE);
        Workflow saved = workflowRepository.save(workflow);
        return convertToResponse(saved);
    }

    public WorkflowResponse deactivate(String id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "工作流不存在"));
        
        if (workflow.getStatus() == WorkflowStatus.INACTIVE) {
            throw new BusinessException(400, "工作流已停用");
        }

        workflow.setStatus(WorkflowStatus.INACTIVE);
        Workflow saved = workflowRepository.save(workflow);
        return convertToResponse(saved);
    }

    private WorkflowResponse convertToResponse(Workflow workflow) {
        List<WorkflowResponse.WorkflowNodeResponse> nodeResponses = workflow.getNodes().stream()
                .map(n -> WorkflowResponse.WorkflowNodeResponse.builder()
                        .id(n.getId())
                        .type(n.getType().getCode())
                        .name(n.getName())
                        .config(n.getConfig())
                        .x(n.getPositionX())
                        .y(n.getPositionY())
                        .agentId(n.getAgentId())
                        .skillId(n.getSkillId())
                        .build())
                .toList();

        List<WorkflowResponse.WorkflowEdgeResponse> edgeResponses = workflow.getEdges().stream()
                .map(e -> WorkflowResponse.WorkflowEdgeResponse.builder()
                        .id(e.getId())
                        .source(e.getSourceNodeId())
                        .target(e.getTargetNodeId())
                        .sourceHandle(e.getSourceHandle())
                        .targetHandle(e.getTargetHandle())
                        .build())
                .toList();

        return WorkflowResponse.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .creatorId(workflow.getCreatorId())
                .createdAt(workflow.getCreatedAt().toString())
                .status(workflow.getStatus().getCode())
                .triggerType(workflow.getTriggerType().getCode())
                .cronExpression(workflow.getCronExpression())
                .executionCount(workflow.getExecutionCount())
                .successRate(workflow.getSuccessRate() != null ? workflow.getSuccessRate().doubleValue() : null)
                .nodes(nodeResponses)
                .edges(edgeResponses)
                .build();
    }
}