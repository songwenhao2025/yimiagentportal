package com.yimi.ai.workflow.service;

import com.yimi.ai.workflow.dto.WorkflowCreateRequest;
import com.yimi.ai.workflow.dto.WorkflowResponse;
import com.yimi.ai.workflow.repository.WorkflowRepository;
import com.yimi.ai.common.entity.Workflow;
import com.yimi.ai.common.enums.TriggerType;
import com.yimi.ai.common.enums.WorkflowStatus;
import com.yimi.ai.common.exception.BusinessException;
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
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

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

    public WorkflowResponse create(WorkflowCreateRequest request) {
        Workflow workflow = Workflow.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .creatorId(request.getCreatorId())
                .status(WorkflowStatus.DRAFT)
                .triggerType(TriggerType.fromCode(request.getTriggerType()))
                .cronExpression(request.getCronExpression())
                .executionCount(0)
                .successRate(0.0)
                .build();

        Workflow saved = workflowRepository.save(workflow);
        return convertToResponse(saved);
    }

    public WorkflowResponse update(String id, WorkflowCreateRequest request) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "工作流不存在"));

        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setTriggerType(TriggerType.fromCode(request.getTriggerType()));
        workflow.setCronExpression(request.getCronExpression());

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
                .successRate(workflow.getSuccessRate())
                .nodes(List.of())
                .edges(List.of())
                .build();
    }
}