package com.yimi.ai.workflow.repository;

import com.yimi.ai.common.entity.WorkflowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, String> {

    List<WorkflowNode> findByWorkflowId(String workflowId);

    void deleteByWorkflowId(String workflowId);
}