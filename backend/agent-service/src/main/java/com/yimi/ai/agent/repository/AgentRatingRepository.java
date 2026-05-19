package com.yimi.ai.agent.repository;

import com.yimi.ai.common.entity.AgentRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRatingRepository extends JpaRepository<AgentRating, String> {
    Page<AgentRating> findByAgentId(String agentId, Pageable pageable);
    Optional<AgentRating> findByAgentIdAndUserId(String agentId, String userId);
    List<AgentRating> findByAgentId(String agentId);
}