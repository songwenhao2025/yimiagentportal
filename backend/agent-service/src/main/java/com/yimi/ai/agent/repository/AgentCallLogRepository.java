package com.yimi.ai.agent.repository;

import com.yimi.ai.common.entity.AgentCallLog;
import com.yimi.ai.common.enums.CallStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentCallLogRepository extends JpaRepository<AgentCallLog, String> {
    Page<AgentCallLog> findByAgentId(String agentId, Pageable pageable);
    Page<AgentCallLog> findByUserId(String userId, Pageable pageable);
    Page<AgentCallLog> findByAgentIdAndStatus(String agentId, CallStatus status, Pageable pageable);
}