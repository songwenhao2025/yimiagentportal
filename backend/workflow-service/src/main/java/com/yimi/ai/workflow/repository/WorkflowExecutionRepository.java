package com.yimi.ai.workflow.repository;

import com.yimi.ai.common.entity.WorkflowExecution;
import com.yimi.ai.common.enums.ExecutionStatus;
import com.yimi.ai.common.enums.TriggerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, String> {
    Page<WorkflowExecution> findByWorkflowId(String workflowId, Pageable pageable);
    Page<WorkflowExecution> findByStatus(ExecutionStatus status, Pageable pageable);
    Page<WorkflowExecution> findByTriggerType(TriggerType triggerType, Pageable pageable);
    Page<WorkflowExecution> findByWorkflowIdAndStatus(String workflowId, ExecutionStatus status, Pageable pageable);
}