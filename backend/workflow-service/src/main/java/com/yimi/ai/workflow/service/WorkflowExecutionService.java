package com.yimi.ai.workflow.service;

import com.yimi.ai.workflow.dto.WorkflowExecutionRequest;
import com.yimi.ai.workflow.dto.WorkflowExecutionResponse;
import com.yimi.ai.workflow.repository.WorkflowExecutionRepository;
import com.yimi.ai.common.entity.WorkflowExecution;
import com.yimi.ai.common.enums.ExecutionStatus;
import com.yimi.ai.common.enums.TriggerType;
import com.yimi.ai.common.exception.BusinessException;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {

    private final WorkflowExecutionRepository executionRepository;

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