package com.yimi.ai.workflow.repository;

import com.yimi.ai.common.entity.Workflow;
import com.yimi.ai.common.enums.WorkflowStatus;
import com.yimi.ai.common.enums.TriggerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, String> {

    Page<Workflow> findByStatus(WorkflowStatus status, Pageable pageable);

    Page<Workflow> findByTriggerType(TriggerType triggerType, Pageable pageable);

    Page<Workflow> findByNameContaining(String name, Pageable pageable);

    List<Workflow> findByCreatorId(String creatorId);

    List<Workflow> findByStatus(WorkflowStatus status);
}