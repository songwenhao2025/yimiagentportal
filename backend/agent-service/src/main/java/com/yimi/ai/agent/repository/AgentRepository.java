package com.yimi.ai.agent.repository;

import com.yimi.ai.common.entity.Agent;
import com.yimi.ai.common.enums.AgentStatus;
import com.yimi.ai.common.enums.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {

    Page<Agent> findByDepartment(Department department, Pageable pageable);

    Page<Agent> findByStatus(AgentStatus status, Pageable pageable);

    Page<Agent> findByDepartmentAndStatus(Department department, AgentStatus status, Pageable pageable);

    Optional<Agent> findByIdAndStatus(String id, AgentStatus status);

    List<Agent> findByCreatorId(String creatorId);

    Page<Agent> findByNameContaining(String name, Pageable pageable);
}