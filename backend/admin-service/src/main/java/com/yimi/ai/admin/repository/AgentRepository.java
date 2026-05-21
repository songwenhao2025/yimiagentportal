package com.yimi.ai.admin.repository;

import com.yimi.ai.common.entity.Agent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {

    @Query("SELECT COUNT(a) FROM Agent a")
    long countAll();

    @Query("SELECT SUM(a.usageCount) FROM Agent a")
    long sumUsageCount();

    @Query("SELECT COUNT(a) FROM Agent a WHERE a.updatedAt BETWEEN :start AND :end")
    long countActiveUsers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Agent a ORDER BY a.usageCount DESC")
    List<Agent> findTopByUsageCount(Pageable pageable);
}
