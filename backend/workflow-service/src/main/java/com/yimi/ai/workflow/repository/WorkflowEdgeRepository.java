package com.yimi.ai.workflow.repository;

import com.yimi.ai.common.entity.WorkflowEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowEdgeRepository extends JpaRepository<WorkflowEdge, String> {

    List<WorkflowEdge> findByWorkflowId(String workflowId);

    void deleteByWorkflowId(String workflowId);
}